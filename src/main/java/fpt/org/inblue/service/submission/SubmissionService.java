package fpt.org.inblue.service.submission;

import fpt.org.inblue.constants.CodeReviewMetricConstant;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.event.SubmissionEventHandle;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.request.CodeReviewEvaluationRequest;
import fpt.org.inblue.model.dto.request.CodeReviewSubmitRequest;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.model.dto.response.CvEvaluationResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final RoundProcessorFactory roundProcessorFactory;
    private final ApplicationService applicationService;
    private final JobDescriptionService jobDescriptionService;
    private final ApiClient apiClient;
    private final ApplicationDetailRepository applicationDetailRepository;

    @Transactional
    public ApplicationDetail evaluateCodeReview(CodeReviewSubmitRequest request) {
        Application currentApplication = applicationService.getApplicationById(request.getApplicationId());
        Round currentRound = jobDescriptionService.getRoundByOrder(
                currentApplication.getJdId(), currentApplication.getCurrentRoundOrder());

        if (currentRound == null) {
            throw new CustomException("Vòng phỏng vấn hiện tại không tồn tại", HttpStatus.NOT_FOUND);
        }

        if (currentRound.getConfigData() == null
                || currentRound.getConfigData().getCodeReviewProblems() == null
                || currentRound.getConfigData().getCodeReviewProblems().isEmpty()) {
            throw new CustomException("Vòng thi không cấu hình bài Code Review nào", HttpStatus.BAD_REQUEST);
        }

        Round.CodeReviewProblemSnapshot problemSnapshot =
                currentRound.getConfigData().getCodeReviewProblems().get(0);

        List<String> defaultMetrics = List.of(
                CodeReviewMetricConstant.BUG_DETECTION,
                CodeReviewMetricConstant.SECURITY_AWARENESS,
                CodeReviewMetricConstant.PERFORMANCE_ANALYSIS,
                CodeReviewMetricConstant.CODE_SMELL_DETECTION,
                CodeReviewMetricConstant.SOLUTION_QUALITY,
                CodeReviewMetricConstant.CLEAN_CODE_AWARENESS,
                CodeReviewMetricConstant.STRENGTH,
                CodeReviewMetricConstant.WEAKNESS,
                CodeReviewMetricConstant.GENERAL_COMMENT,
                CodeReviewMetricConstant.MISSED_ISSUES);
        CodeReviewEvaluationRequest.EvaluationCriteria criteriaDto =
                CodeReviewEvaluationRequest.EvaluationCriteria.builder()
                        .maxScore(currentRound.getConfigData().getMaxScore())
                        .aiSystemPrompt(currentRound.getConfigData().getAiSystemPrompt())
                        .extraMetrics(defaultMetrics)
                        .build();

        CodeReviewEvaluationRequest.CodeReviewProblem problemDto =
                CodeReviewEvaluationRequest.CodeReviewProblem.builder()
                        .title(problemSnapshot.getTitle())
                        .difficulty(
                                problemSnapshot.getDifficulty() != null
                                        ? problemSnapshot.getDifficulty().name()
                                        : "MEDIUM")
                        .language(problemSnapshot.getLanguage())
                        .problemStatement(problemSnapshot.getProblemStatement())
                        .files(problemSnapshot.getFiles())
                        .expectedIssues(problemSnapshot.getExpectedIssues())
                        .build();

        CodeReviewEvaluationRequest evaluationRequest = CodeReviewEvaluationRequest.builder()
                .evaluationCriteria(criteriaDto)
                .codeReviewProblem(problemDto)
                .submissions(request.getSubmissions())
                .build();

        CvEvaluationResponse response = apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.CODE_REVIEW,
                evaluationRequest,
                "java-backend" + request.getApplicationId(),
                false,
                null,
                CvEvaluationResponse.class);

        if (response == null) {
            throw new CustomException("AI Service không phản hồi kết quả chấm điểm", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<ApplicationDetail> detailOpt = applicationDetailRepository.findByApplicationIdAndRoundId(
                request.getApplicationId(), currentRound.getId());

        ApplicationDetail detail = detailOpt.orElseGet(() -> ApplicationDetail.builder()
                .applicationId(request.getApplicationId())
                .roundId(currentRound.getId())
                .build());

        ApplicationDetail.SubmissionData submissionData = detail.getSubmissionData() != null
                ? detail.getSubmissionData()
                : new ApplicationDetail.SubmissionData();
        submissionData.setCodeReviewSubmissions(request.getSubmissions());
        detail.setSubmissionData(submissionData);

        detail.setAiScore(response.getScore());
        detail.setFinalScore(response.getScore());
        detail.setAiFeedback(SubmissionEventHandle.parseRawMetrics(response.getExtraMetrics()));
        detail.setStatus(ApplicationDetailStatus.AI_EVALUATED);

        ApplicationDetail.RoundResult roundResult = response.getScore() >= currentRound.getPassThreshold()
                ? ApplicationDetail.RoundResult.PASSED
                : ApplicationDetail.RoundResult.FAILED;
        detail.setFinalResult(roundResult);
      //  applicationService.moveToNextRound(currentApplication);
        return applicationDetailRepository.save(detail);
    }

    @Transactional
    public SubmissionResult submitRound(SubmitRequest detail) throws IOException {
        Application currentApplication = applicationService.getApplicationById(detail.getApplicationId());
        Round currentRound = jobDescriptionService.getRoundByOrder(
                currentApplication.getJdId(), currentApplication.getCurrentRoundOrder());
        RoundSubmissionProcessor processor = roundProcessorFactory.getProcessor(currentRound.getRoundType());
        ProcessDto processDto = new ProcessDto();
        processDto.setApplication(currentApplication);
        processDto.setRound(currentRound);
        processDto.setFile(detail.getFile());
        processDto.setQuizAnswers(detail.getQuizAnswers());
        processDto.setTextContent(detail.getTextContent());
        processDto.setRoundType(currentRound.getRoundType());
        processDto.setCompileRequest(detail.getCompileRequest());
        SubmissionResult submissionResult = processor.process(processDto);
        //
        // if(submissionResult.getStatus().equals(SubmissionResult.Status.COMPLETED)&&submissionResult.getRoundResult().equals(RoundResult.PASSED)){
        //            applicationService.moveToNextRound(currentApplication);
        //        }
        return submissionResult;
    }
}
