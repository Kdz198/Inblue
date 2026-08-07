package fpt.org.inblue.service.impl;

import fpt.org.inblue.Config.SwecomMappingConfig;
import fpt.org.inblue.enums.CompetencyLevel;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.response.BehavioralSkillScore;
import fpt.org.inblue.model.dto.response.CompetencyChartResponse;
import fpt.org.inblue.model.dto.response.SkillAreaScore;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.CompetencyChartService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetencyChartServiceImpl implements CompetencyChartService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final RoundRepository roundRepository;
    private final UserRepository userRepository;

    public CompetencyChartServiceImpl(
            ApplicationRepository applicationRepository,
            ApplicationDetailRepository applicationDetailRepository,
            JobDescriptionRepository jobDescriptionRepository,
            RoundRepository roundRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationDetailRepository = applicationDetailRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.roundRepository = roundRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CompetencyChartResponse getCompetencyChart(Long applicationId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
        JobDescription jobDescription = jobDescriptionRepository
                .findById(application.getJdId())
                .orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));
        User candidate = userRepository
                .findById(application.getUserId())
                .orElseThrow(() -> new CustomException("Candidate not found", HttpStatus.NOT_FOUND));

        List<ApplicationDetail> details = applicationDetailRepository.findAllByApplicationId(applicationId);
        Map<Long, Round> roundsById = roundRepository
                .findAllById(details.stream()
                        .map(ApplicationDetail::getRoundId)
                        .filter(roundId -> roundId != null)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Round::getId, Function.identity()));

        List<RoundContribution> contributions = details.stream()
                .filter(detail -> detail.getRoundId() != null && roundsById.containsKey(detail.getRoundId()))
                .map(detail -> buildContribution(detail, roundsById.get(detail.getRoundId())))
                .filter(contribution -> contribution.score() != null)
                .toList();

        Double overallScore = resolveOverallScore(application, contributions);

        return CompetencyChartResponse.builder()
                .applicationId(applicationId)
                .candidateName(candidate.getName())
                .jobTitle(jobDescription.getTitle())
                .overallScore(overallScore)
                .overallLevel(toCompetencyLevel(overallScore))
                .technicalSkillAreas(buildTechnicalSkillAreas(contributions))
                .behavioralSkills(buildBehavioralSkills(contributions))
                .build();
    }

    private List<SkillAreaScore> buildTechnicalSkillAreas(List<RoundContribution> contributions) {
        Map<String, List<RoundContribution>> contributionsByArea = new LinkedHashMap<>();

        for (RoundContribution contribution : contributions) {
            List<String> skillAreas = SwecomMappingConfig.TECHNICAL_MAPPING.get(contribution.roundType());
            if (skillAreas == null) {
                continue;
            }
            for (String skillArea : skillAreas) {
                contributionsByArea
                        .computeIfAbsent(skillArea, key -> new ArrayList<>())
                        .add(contribution);
            }
        }

        return contributionsByArea.entrySet().stream()
                .map(entry -> {
                    Double score = average(entry.getValue());
                    return SkillAreaScore.builder()
                            .skillArea(entry.getKey())
                            .score(score)
                            .level(toCompetencyLevel(score))
                            .sourceRounds(sourceRounds(entry.getValue()))
                            .build();
                })
                .sorted(Comparator.comparing(SkillAreaScore::getSkillArea))
                .toList();
    }

    private List<BehavioralSkillScore> buildBehavioralSkills(List<RoundContribution> contributions) {
        Map<String, List<RoundContribution>> contributionsBySkill = new LinkedHashMap<>();

        for (RoundContribution contribution : contributions) {
            String skillName = SwecomMappingConfig.BEHAVIORAL_MAPPING.get(contribution.roundType());
            if (skillName == null) {
                continue;
            }
            contributionsBySkill
                    .computeIfAbsent(skillName, key -> new ArrayList<>())
                    .add(contribution);
        }

        return contributionsBySkill.entrySet().stream()
                .map(entry -> BehavioralSkillScore.builder()
                        .skillName(entry.getKey())
                        .score(average(entry.getValue()))
                        .sourceRounds(sourceRounds(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(BehavioralSkillScore::getSkillName))
                .toList();
    }

    private RoundContribution buildContribution(ApplicationDetail detail, Round round) {
        Double rawScore = resolveRawScore(detail);
        Double normalizedScore = normalizeScore(rawScore, round);
        return new RoundContribution(round.getRoundType(), roundName(round), normalizedScore);
    }

    private Double resolveRawScore(ApplicationDetail detail) {
        if (detail.getFinalScore() != null) {
            return detail.getFinalScore();
        }
        if (detail.getAiScore() != null) {
            return detail.getAiScore();
        }
        return detail.getHrScore();
    }

    private Double normalizeScore(Double rawScore, Round round) {
        if (rawScore == null) {
            return null;
        }
        if (round.getRoundType() == RoundType.AI_INTERVIEW && rawScore <= 10) {
            return clamp(Math.round(rawScore * 10.0 * 100.0) / 100.0);
        }
        Integer maxScore = round.getConfigData() != null ? round.getConfigData().getMaxScore() : null;
        if (maxScore != null && maxScore > 0) {
            return clamp(Math.round((rawScore / maxScore) * 10000.0) / 100.0);
        }
        return clamp(rawScore);
    }

    private Double resolveOverallScore(Application application, List<RoundContribution> contributions) {
        if (application.getOverallScore() != null && application.getOverallScore() >= 0) {
            return clamp(application.getOverallScore());
        }
        if (contributions.isEmpty()) {
            return null;
        }
        return average(contributions);
    }

    private Double average(List<RoundContribution> contributions) {
        return Math.round(contributions.stream()
                                .map(RoundContribution::score)
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                        * 100.0)
                / 100.0;
    }

    private List<String> sourceRounds(List<RoundContribution> contributions) {
        return contributions.stream()
                .map(RoundContribution::roundName)
                .distinct()
                .toList();
    }

    private String roundName(Round round) {
        if (round.getName() != null && !round.getName().isBlank()) {
            return round.getName();
        }
        return round.getRoundType() != null ? round.getRoundType().name() : "Unknown Round";
    }

    private Double clamp(Double score) {
        if (score == null) {
            return null;
        }
        if (score < 0) {
            return 0.0;
        }
        if (score > 100) {
            return 100.0;
        }
        return score;
    }

    private CompetencyLevel toCompetencyLevel(Double score) {
        if (score == null || score < 40) {
            return CompetencyLevel.TECHNICIAN;
        }
        if (score < 55) {
            return CompetencyLevel.ENTRY_LEVEL_PRACTITIONER;
        }
        if (score < 70) {
            return CompetencyLevel.PRACTITIONER;
        }
        if (score < 85) {
            return CompetencyLevel.TECHNICAL_LEADER;
        }
        return CompetencyLevel.SENIOR_SOFTWARE_ENGINEER;
    }

    private record RoundContribution(RoundType roundType, String roundName, Double score) {}
}
