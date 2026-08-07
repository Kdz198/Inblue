package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.model.dto.request.JourneySummaryAIRequest;
import fpt.org.inblue.model.dto.response.CompetencyChartResponse;
import fpt.org.inblue.model.dto.response.JourneySummaryAIResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JourneySummaryRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.CompetencyChartService;
import fpt.org.inblue.service.JourneySummaryService;
import fpt.org.inblue.service.summary.RoundSummaryService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourneySummaryServiceImpl implements JourneySummaryService {
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final RoundRepository roundRepository;
    private final JourneySummaryRepository journeySummaryRepository;
    private final UserRepository userRepository;
    private final RoundSummaryService roundSummaryService;
    private final CompetencyChartService competencyChartService;
    private final ApiClient apiClient;

    @Value("${SUMMARY_REPORT_FALLBACK_ENABLED:true}")
    private boolean summaryReportFallbackEnabled;

    @Override
    @Transactional
    public JourneySummaryAIRequest generate(Long applicationId) {
        log.info("Building journey summary AI request for applicationId={}", applicationId);
        JourneySummaryAIRequest request = buildAIRequest(applicationId);
        int roundSummaryCount = request.getData() != null && request.getData().getRoundSummaries() != null
                ? request.getData().getRoundSummaries().size()
                : 0;
        log.info(
                "Calling AnythingLLM SUMMARY_REPORT workspace. applicationId={}, workspaceSlug={}, roundSummaryCount={}",
                applicationId,
                AnythingLlmWorkspace.SUMMARY_REPORT.getSlug(),
                roundSummaryCount);
        try {
            JourneySummaryAIResponse response = apiClient.sendChatToAnythingLlm(
                    AnythingLlmWorkspace.SUMMARY_REPORT,
                    request,
                    "summary-report-" + applicationId,
                    true,
                    null,
                    JourneySummaryAIResponse.class);

            if (response == null || response.getNarrative() == null || response.getNarrative().isBlank()) {
                throw new CustomException("AI summary report response is empty", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info(
                    "Received SUMMARY_REPORT response. applicationId={}, hasCompetencyChart={}, swecomAssessmentCount={}, recommendationCount={}",
                    applicationId,
                    response.getCompetencyChart() != null,
                    response.getSwecomAssessments() != null ? response.getSwecomAssessments().size() : 0,
                    response.getDevelopmentRecommendations() != null
                            ? response.getDevelopmentRecommendations().size()
                            : 0);
            saveAIResponse(applicationId, request, response);
            log.info("Journey summary generated for applicationId={}", applicationId);
        } catch (Exception e) {
            if (!summaryReportFallbackEnabled) {
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            saveFallbackSummary(applicationId, request, e);
            log.warn("Journey summary fallback generated for applicationId={}", applicationId, e);
        }
        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public JourneySummaryAIRequest buildAIRequest(Long applicationId) {
        AISummaryRequest summaryRequest = buildSummaryRequest(applicationId);
        return JourneySummaryAIRequest.builder()
                .data(summaryRequest)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AISummaryRequest buildSummaryRequest(Long applicationId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
        JobDescription jobDescription = jobDescriptionRepository
                .findById(application.getJdId())
                .orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));

        List<ApplicationDetail> details = applicationDetailRepository.findAllByApplicationId(applicationId);
        Map<Long, Round> roundsById = roundRepository.findAllById(details.stream()
                        .map(ApplicationDetail::getRoundId)
                        .filter(roundId -> roundId != null)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Round::getId, Function.identity()));

        List<AISummaryRequest.RoundSummaryInfo> roundSummaries = details.stream()
                .filter(detail -> detail.getRoundId() != null && roundsById.containsKey(detail.getRoundId()))
                .sorted(Comparator.comparing(detail -> resolveRoundOrder(detail, roundsById)))
                .map(detail -> roundSummaryService.buildRoundSummary(detail, roundsById.get(detail.getRoundId())))
                .toList();

        return AISummaryRequest.builder()
                .jobDescription(AISummaryRequest.JobDescriptionDto.builder()
                        .title(jobDescription.getTitle())
                        .level(jobDescription.getLevel() != null ? jobDescription.getLevel().name() : null)
                        .keyRequirements(extractKeyRequirements(jobDescription.getRequirements()))
                        .build())
                .roundSummaries(roundSummaries)
                .build();
    }

    @Override
    @Transactional
    public JourneySummary saveNarrative(Long applicationId, String narrative) {
        if (narrative == null || narrative.isBlank()) {
            throw new CustomException("Journey narrative cannot be empty", HttpStatus.BAD_REQUEST);
        }

        JourneySummary summary = JourneySummary.builder()
                .applicationId(applicationId)
                .narrative(narrative.trim())
                .generatedAt(LocalDateTime.now())
                .build();
        return journeySummaryRepository.save(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public JourneySummary getSavedSummary(Long applicationId) {
        return journeySummaryRepository
                .findTopByApplicationIdOrderByGeneratedAtDesc(applicationId)
                .orElseThrow(() -> new CustomException(
                        "Journey summary is not available yet. Candidate may not have completed all rounds.",
                        HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CompetencyChartResponse getSavedCompetencyChart(Long applicationId) {
        JourneySummary summary = getSavedSummary(applicationId);
        if (summary.getCompetencyChart() == null) {
            throw new CustomException(
                    "Competency chart is not available yet. Summary report may not have been generated by AI.",
                    HttpStatus.NOT_FOUND);
        }
        return summary.getCompetencyChart();
    }

    private JourneySummary saveAIResponse(
            Long applicationId, JourneySummaryAIRequest request, JourneySummaryAIResponse response) {
        enrichCompetencyChartMetadata(applicationId, request, response.getCompetencyChart());

        JourneySummary summary = JourneySummary.builder()
                .applicationId(applicationId)
                .narrative(response.getNarrative().trim())
                .competencyChart(response.getCompetencyChart())
                .swecomAssessments(response.getSwecomAssessments())
                .developmentRecommendations(response.getDevelopmentRecommendations())
                .generatedAt(LocalDateTime.now())
                .build();
        JourneySummary savedSummary = journeySummaryRepository.save(summary);
        log.info(
                "Saved AI journey summary. applicationId={}, journeySummaryId={}, hasCompetencyChart={}",
                applicationId,
                savedSummary.getId(),
                savedSummary.getCompetencyChart() != null);
        return savedSummary;
    }

    private JourneySummary saveFallbackSummary(Long applicationId, JourneySummaryAIRequest request, Exception cause) {
        CompetencyChartResponse competencyChart = competencyChartService.getCompetencyChart(applicationId);
        enrichCompetencyChartMetadata(applicationId, request, competencyChart);

        JourneySummary summary = JourneySummary.builder()
                .applicationId(applicationId)
                .narrative(buildFallbackNarrative(cause))
                .competencyChart(competencyChart)
                .generatedAt(LocalDateTime.now())
                .build();
        JourneySummary savedSummary = journeySummaryRepository.save(summary);
        log.info(
                "Saved fallback journey summary. applicationId={}, journeySummaryId={}, hasCompetencyChart={}",
                applicationId,
                savedSummary.getId(),
                savedSummary.getCompetencyChart() != null);
        return savedSummary;
    }

    private String buildFallbackNarrative(Exception cause) {
        String reason = cause.getMessage();
        if (reason == null || reason.isBlank()) {
            reason = "AI summary report service is not available";
        }
        return "Báo cáo AI chưa được sinh vì dịch vụ SUMMARY_REPORT chưa phản hồi hợp lệ. "
                + "Dữ liệu biểu đồ năng lực hiện được dựng tạm từ điểm các vòng đã hoàn thành. "
                + "Lý do kỹ thuật: " + reason;
    }

    private void enrichCompetencyChartMetadata(
            Long applicationId, JourneySummaryAIRequest request, CompetencyChartResponse competencyChart) {
        if (competencyChart == null) {
            return;
        }

        competencyChart.setApplicationId(applicationId);
        if (competencyChart.getJobTitle() == null || competencyChart.getJobTitle().isBlank()) {
            AISummaryRequest.JobDescriptionDto jobDescription =
                    request.getData() != null ? request.getData().getJobDescription() : null;
            if (jobDescription != null) {
                competencyChart.setJobTitle(jobDescription.getTitle());
            }
        }

        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return;
        }

        userRepository.findById(application.getUserId()).ifPresent(candidate -> {
            if (candidate.getName() != null && !candidate.getName().isBlank()) {
                competencyChart.setCandidateName(candidate.getName());
            }
        });
    }

    private List<String> extractKeyRequirements(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            return List.of();
        }

        return requirements.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.substring(1).trim())
                .filter(line -> !line.isBlank())
                .limit(5)
                .toList();
    }

    private Integer resolveRoundOrder(ApplicationDetail detail, Map<Long, Round> roundsById) {
        Round round = roundsById.get(detail.getRoundId());
        if (round == null || round.getRoundOrder() == null) {
            return Integer.MAX_VALUE;
        }
        return round.getRoundOrder();
    }
}
