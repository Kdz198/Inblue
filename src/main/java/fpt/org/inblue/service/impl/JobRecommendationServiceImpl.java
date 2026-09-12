package fpt.org.inblue.service.impl;

import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.model.UserCompetency;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.mapper.JobRecommendationMapper;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.JobRecommendationConfig;
import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import fpt.org.inblue.model.dto.response.JobRecommendationThresholdResponse;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JobRecommendationConfigRepository;
import fpt.org.inblue.service.JobRecommendationService;
import fpt.org.inblue.utils.VectorUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationServiceImpl implements JobRecommendationService {
    private static final int RECOMMENDATION_CANDIDATE_LIMIT = 100;

    private final UserCareerPreferenceRepository preferenceRepository;
    private final UserCompetencyRepository competencyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobRecommendationConfigRepository configRepository;
    private final JobRecommendationMapper jobRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobRecommendationResponse> getRecommendations(int userId) {
        Optional<UserCareerPreference> preferenceOptional = preferenceRepository.findByUserIdAndIsActiveTrue(userId);
        Optional<JobRecommendationConfig> configOptional =
                configRepository.findById(JobRecommendationConfig.SINGLETON_ID);

        if (preferenceOptional.isEmpty() || configOptional.isEmpty()) {
            log.info(
                    "Job recommendations unavailable: userId={}, hasPreference={}, hasThresholdConfig={}",
                    userId,
                    preferenceOptional.isPresent(),
                    configOptional.isPresent());
            return List.of();
        }

        UserCareerPreference preference = preferenceOptional.get();
        float[] preferenceVector = preference.getSkillEmbedding();
        if (preferenceVector == null || preferenceVector.length == 0) {
            log.info("Job recommendations unavailable: userId={} has no skill embedding", userId);
            return List.of();
        }
        Optional<UserCompetency> competencyOptional =
                competencyRepository.findByUser_IdAndCareerPreferenceId(userId, preference.getUserId());
        if (competencyOptional.isEmpty() || competencyOptional.get().getCurrentLevel() == null) {
            log.info(
                    "Job recommendations unavailable: userId={} hasCurrentCompetency={}, hasCurrentLevel={}",
                    userId,
                    competencyOptional.isPresent(),
                    competencyOptional.map(UserCompetency::getCurrentLevel).isPresent());
            return List.of();
        }
        TargetLevel currentLevel = competencyOptional.get().getCurrentLevel();

        double threshold = configOptional.get().getMatchThresholdPercent().doubleValue();
        LocalDateTime now = LocalDateTime.now();
        String vectorStr = Arrays.toString(preferenceVector);
        List<JobDescription> candidateJobs =
                jobDescriptionRepository.findTopRecommendedJobs(vectorStr, RECOMMENDATION_CANDIDATE_LIMIT);
        List<ScoredJob> scoredJobs = new ArrayList<>();

        for (JobDescription job : candidateJobs) {
            boolean eligible = isEligible(job, now);

            float[] jobVector = job.getSkillEmbedding();
            if (jobVector == null || jobVector.length == 0) {
                log.info(
                        "Bỏ qua so sánh JD vì chưa có vector: jdId={}, title='{}', status={}, deadlineAt={}, eligible={}",
                        job.getId(),
                        job.getTitle(),
                        job.getStatus(),
                        job.getDeadlineAt(),
                        eligible);
                continue;
            }

            scoredJobs.add(scoreJob(userId, job, preferenceVector, currentLevel, threshold, eligible));
        }

        log.info(
                "Job recommendation candidates: userId={}, candidateJobCount={}, scoredJobCount={}, thresholdPercent={}",
                userId,
                candidateJobs.size(),
                scoredJobs.size(),
                threshold);

        List<JobRecommendationResponse> recommendations = scoredJobs.stream()
                .filter(scoredJob ->
                        scoredJob.eligible() && scoredJob.levelMatched() && scoredJob.score() >= threshold)
                .sorted(Comparator.comparingDouble(ScoredJob::score).reversed())
                .map(this::toResponse)
                .toList();
        log.info("Job recommendations completed: userId={}, matchedJobCount={}", userId, recommendations.size());
        return recommendations;
    }

    @Override
    @Transactional
    public JobRecommendationThresholdResponse updateThreshold(BigDecimal thresholdPercent) {
        JobRecommendationConfig config = configRepository
                .findById(JobRecommendationConfig.SINGLETON_ID)
                .orElseGet(() -> JobRecommendationConfig.builder()
                        .id(JobRecommendationConfig.SINGLETON_ID)
                        .build());
        config.setMatchThresholdPercent(thresholdPercent);
        JobRecommendationConfig saved = configRepository.save(config);
        return JobRecommendationThresholdResponse.builder()
                .thresholdPercent(saved.getMatchThresholdPercent())
                .build();
    }

    private boolean isEligible(JobDescription job, LocalDateTime now) {
        return job.getStatus() == JobDescriptionStatus.OPEN
                && Boolean.FALSE.equals(job.getIsDeleted())
                && (job.getDeadlineAt() == null || !job.getDeadlineAt().isBefore(now));
    }

    private ScoredJob scoreJob(
            int userId,
            JobDescription job,
            float[] preferenceVector,
            TargetLevel currentLevel,
            double threshold,
            boolean eligible) {
        log.info(
                "Bắt đầu so sánh vector: userId={}, jdId={}, userVectorDimension={}, jdVectorDimension={}",
                userId,
                job.getId(),
                preferenceVector.length,
                job.getSkillEmbedding().length);
        double score = VectorUtils.cosineSimilarity(preferenceVector, job.getSkillEmbedding());
        boolean levelMatched = job.getLevel() != null && job.getLevel() == currentLevel;
        log.info(
                "Kết quả matching JD: userId={}, jdId={}, title='{}', currentLevel={}, jobLevel={}, levelMatched={}, status={}, deadlineAt={}, matchPercent={}%, thresholdPercent={}%, eligible={}, matched={}",
                userId,
                job.getId(),
                job.getTitle(),
                currentLevel,
                job.getLevel(),
                levelMatched,
                job.getStatus(),
                job.getDeadlineAt(),
                score,
                threshold,
                eligible,
                eligible && levelMatched && score >= threshold);
        return new ScoredJob(job, score, eligible, levelMatched);
    }

    private JobRecommendationResponse toResponse(ScoredJob scoredJob) {
        return jobRecommendationMapper.toResponse(scoredJob.job(), scoredJob.score());
    }

    private record ScoredJob(JobDescription job, double score, boolean eligible, boolean levelMatched) {}
}
