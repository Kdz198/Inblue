package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.constants.CvMetricsConstant;
import fpt.org.inblue.constants.EmailMetricsConstant;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.request.CompileRequest;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.model.dto.request.CvEvaluationRequest;
import fpt.org.inblue.model.dto.request.EmailEvaluationRequest;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.model.dto.response.CvEvaluationResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.UserService;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionEventHandle {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApiClient ApiClient;
    private final CloudinaryService cloudinaryService;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ObjectMapper objectMapper;
    private final CodingProblemsRepository codingProblemsRepository;
    private final ApiClient apiClient;
    private final ApplicationService applicationService;
    private final EmailSubmissionRepository emailSubmissionRepository;
    private final UserService userService;

    @Async
    @EventListener
    public void handleEventSubmission(ProcessDto dto) throws IOException {
        switch (dto.getRoundType()) {
            case CV_SCREENING -> processCvSubmission(dto);
            case EMAIL_SIMULATOR -> processEmailSubmission(dto);
            case CODING -> processCodeSubmission(dto);
            default -> throw new CustomException(
                    "Unsupported round type: " + dto.getRoundType(), HttpStatus.BAD_REQUEST);
        }
    }

    private void processCodeSubmission(ProcessDto dto) {
        Round round = dto.getRound();

        List<CompileRequest> compileRequests = dto.getCompileRequest();
        if (compileRequests == null || compileRequests.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy thông tin yêu cầu compile bài toán (CompileRequest là null hoặc rỗng)");
        }

        double totalScore = 0.0;
        int totalPassed = 0;
        int totalTests = 0;
        long totalTime = 0;
        String combinedError = null;
        List<ApplicationDetail.CodeSubmission> codeSubmissions = new ArrayList<>();
        StringBuilder sourceCodes = new StringBuilder();

        for (CompileRequest compileRequest : compileRequests) {
            CodingProblem problem = codingProblemsRepository
                    .findById(compileRequest.getProblemId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy bài toán mã số: " + compileRequest.getProblemId()));

            // Nạp toàn bộ các Test Case Ẩn (Hidden Test Cases) để chấm điểm tuyệt đối
            List<CompilerRequestDto.TestCase> testcases = new ArrayList<>();
            if (problem.getHiddenTestCases() != null) {
                for (CodingProblem.TestCase testCase : problem.getHiddenTestCases()) {
                    CompilerRequestDto.TestCase testCaseDto = CompilerRequestDto.TestCase.builder()
                            .expectedOutput(testCase.getExpectedOutput())
                            .inputs(testCase.getInputs())
                            .build();
                    testcases.add(testCaseDto);
                }
            }

            // Đóng gói Payload gửi sang hệ thống Sandbox Compile Code
            CompilerRequestDto requestDto = CompilerRequestDto.builder()
                    .language(compileRequest.getLanguage())
                    .memoryLimitMb(problem.getMemoryLimitMb())
                    .sourceCode(compileRequest.getSourceCode())
                    .testCases(testcases)
                    .timeLimitMs(problem.getExecutionTimeLimitMs())
                    .paramTypes(problem.getParamTypes())
                    .returnType(problem.getReturnType())
                    .build();

            // Gọi Client Service thực thi code, nhận kết quả trả về từ Sandbox
            CompilerResponseDto response = apiClient.executeCode(requestDto);
            if (response == null) {
                continue; // bỏ qua nếu sandbox không trả về kết quả
            }

            // Tính điểm từng bài
            double score = 0.0;
            if (response.getTestCases() != null) {
                for (CompilerResponseDto.TestCaseResult testCaseResult : response.getTestCases()) {
                    if ("PASSED".equals(testCaseResult.getStatus())
                            && problem.getHiddenTestCases() != null
                            && testCaseResult.getIndex() >= 0
                            && testCaseResult.getIndex()
                                    < problem.getHiddenTestCases().size()) {
                        CodingProblem.TestCase testCase =
                                problem.getHiddenTestCases().get(testCaseResult.getIndex());
                        score += testCase.getWeightPoints();
                    }
                }
            }

            totalScore += score;
            totalPassed += response.getPassedTestCases();
            totalTests += response.getTotalTestCases();
            totalTime += response.getExecutionTimeMs();
            if (response.getErrorMessage() != null
                    && !response.getErrorMessage().isEmpty()) {
                combinedError = combinedError == null
                        ? response.getErrorMessage()
                        : combinedError + "; " + response.getErrorMessage();
            }

            // Lưu source code từng bài vào CodeSubmission
            codeSubmissions.add(ApplicationDetail.CodeSubmission.builder()
                    .sourceCode(compileRequest.getSourceCode())
                    .testCases(response)
                    .build());

            // Gộp source code thành chuỗi để lưu textContent
            if (sourceCodes.length() > 0) {
                sourceCodes
                        .append("\n\n// --- Problem ")
                        .append(compileRequest.getProblemId())
                        .append(" ---\n");
            } else {
                sourceCodes
                        .append("// --- Problem ")
                        .append(compileRequest.getProblemId())
                        .append(" ---\n");
            }
            sourceCodes.append(
                    compileRequest.getSourceCode() != null ? String.join("\n", compileRequest.getSourceCode()) : "");
        }

        ApplicationDetail.RoundResult roundResult = totalScore >= round.getPassThreshold()
                ? ApplicationDetail.RoundResult.PASSED
                : ApplicationDetail.RoundResult.FAILED;
        ApplicationDetail.SubmissionData submissionData = ApplicationDetail.SubmissionData.builder()
                .textContent(sourceCodes.toString())
                .codeSubmissions(codeSubmissions)
                .build();
        ApplicationDetail applicationDetail = ApplicationDetail.builder()
                .applicationId(dto.getApplication().getId())
                .roundId(dto.getRound().getId())
                .status(ApplicationDetailStatus.COMPLETED)
                .finalScore(totalScore)
                .finalResult(roundResult)
                .submissionData(submissionData)
                .build();

        applicationDetailRepository.save(applicationDetail);

        applicationService.moveToNextRound(dto.getApplication());
    }

    private void processEmailSubmission(ProcessDto dto) {
        Long applicationId = dto.getApplication().getId();
        List<EmailSubmission> submissions = emailSubmissionRepository.findByStatusAndApplicationIdOrderByIdDesc(
                EmailSubmission.EmailStatus.PROCESSED, applicationId);
        if (submissions.isEmpty()) {
            log.warn("No PROCESSED email submission found for application ID: {}", applicationId);
            return;
        }
        EmailSubmission submission = submissions.get(0);

        try {
            Round round = dto.getRound();
            Optional<JobDescription> jobDescription =
                    jobDescriptionRepository.findById(dto.getApplication().getJdId());
            if (jobDescription.isEmpty()) {
                throw new CustomException(
                        "Job Description not found for id: "
                                + dto.getApplication().getJdId(),
                        HttpStatus.NOT_FOUND);
            }
            List<String> criteria = new ArrayList<>(List.of(
                    EmailMetricsConstant.CLOSING_AND_SIGNATURE, EmailMetricsConstant.FORMATTING_AND_STRUCTURE,
                    EmailMetricsConstant.CONTENT_AND_CLARITY, EmailMetricsConstant.GRAMMAR_AND_VOCABULARY,
                    EmailMetricsConstant.GENERAL_COMMENT, EmailMetricsConstant.SALUTATION_AND_OPENING,
                    EmailMetricsConstant.STRENGTH, EmailMetricsConstant.SUBJECT_LINE_QUALITY,
                    EmailMetricsConstant.TONE_AND_PROFESSIONALISM, EmailMetricsConstant.WEAKNESS));
            EmailEvaluationRequest.EvaluationCriteria evaluation = EmailEvaluationRequest.EvaluationCriteria.builder()
                    .maxScore(round.getConfigData().getMaxScore())
                    .aiSystemPrompt(round.getConfigData().getAiSystemPrompt())
                    .extraMetrics(criteria)
                    .build();
            EmailEvaluationRequest.SubmitDto submitDto = EmailEvaluationRequest.SubmitDto.builder()
                    .senderEmail(submission.getSenderEmail())
                    .subject(submission.getSubject())
                    .bodyText(submission.getBodyText())
                    .attachmentUrls(submission.getAttachmentUrls())
                    .build();

            EmailEvaluationRequest.EmailContext context = EmailEvaluationRequest.EmailContext.builder()
                    .scenario(round.getConfigData().getEvaluationCriteria())
                    .level(String.valueOf(jobDescription.get().getLevel()))
                    .candidateEmail(submitDto)
                    .build();
            EmailEvaluationRequest emailEvaluationRequest = EmailEvaluationRequest.builder()
                    .emailContext(context)
                    .evaluationCriteria(evaluation)
                    .build();
            // Gọi LLM API để chấm điểm email
            CvEvaluationResponse response = ApiClient.sendChatToAnythingLlm(
                    AnythingLlmWorkspace.EMAIL,
                    emailEvaluationRequest,
                    "java-backend",
                    false,
                    null,
                    CvEvaluationResponse.class);

            ApplicationDetail applicationDetail = new ApplicationDetail();
            applicationDetail.setApplicationId(dto.getApplication().getId());
            applicationDetail.setRoundId(dto.getRound().getId());
            applicationDetail.setStatus(ApplicationDetailStatus.AI_EVALUATED);
            ApplicationDetail.SubmissionData submissionData = ApplicationDetail.SubmissionData.builder()
                    .emailSubmissionId(submission.getId())
                    .build();
            applicationDetail.setSubmissionData(submissionData);
            applicationDetail.setAiScore(response.getScore());
            applicationDetail.setAiFeedback(parseRawMetrics(response.getExtraMetrics()));
            applicationDetailRepository.save(applicationDetail);

            submission.setStatus(EmailSubmission.EmailStatus.PROCESSED);
            emailSubmissionRepository.save(submission);
            log.info("Successfully evaluated and saved email submission for applicationId: {}", applicationId);
        } catch (Exception e) {
            log.error("Failed to process email submission for application ID: " + applicationId, e);
            submission.setStatus(EmailSubmission.EmailStatus.ERROR);
            submission.setErrorMessage(e.getMessage());
            emailSubmissionRepository.save(submission);
        }
    }

    private void processCvSubmission(ProcessDto dto) throws IOException {
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            System.err.println("File not found");
        } else {
            userService.upCv(dto.getApplication().getUserId(), dto.getFile());
        }
        Round round = dto.getRound();
        Optional<JobDescription> jobDescription =
                jobDescriptionRepository.findById(dto.getApplication().getJdId());
        if (jobDescription.isEmpty()) {
            throw new CustomException(
                    "Job Description not found for id: " + dto.getApplication().getJdId(), HttpStatus.NOT_FOUND);
        }
        List<String> criteria = new ArrayList<>(List.of(
                CvMetricsConstant.CV_READABILITY_SCORE,
                CvMetricsConstant.EDUCATION_MATCH_SCORE,
                CvMetricsConstant.EXPERIENCE_MATCH_SCORE,
                CvMetricsConstant.KEYWORD_DENSITY,
                CvMetricsConstant.OVERALL_CV_MATCH,
                CvMetricsConstant.POTENTIAL_RED_FLAGS,
                CvMetricsConstant.SKILLS_MATCH_SCORE,
                CvMetricsConstant.STRENGTH,
                CvMetricsConstant.WEAKNESS,
                CvMetricsConstant.GENERAL_COMMENT));
        CvEvaluationRequest.EvaluationCriteria evaluation = CvEvaluationRequest.EvaluationCriteria.builder()
                .maxScore(round.getConfigData().getMaxScore())
                .aiSystemPrompt(round.getConfigData().getAiSystemPrompt())
                .extraMetrics(criteria)
                .build();
        CvEvaluationRequest.JD jd = CvEvaluationRequest.JD
                .builder()
                .title(jobDescription.get().getTitle())
                .description(jobDescription.get().getDescription())
                .level(String.valueOf(jobDescription.get().getLevel()))
                .requirements(jobDescription.get().getRequirements())
                .build();
        CvEvaluationRequest cvEvaluationRequest = CvEvaluationRequest.builder()
                .cvFile(dto.getFile())
                .evaluationCriteria(evaluation)
                .jobDescription(jd)
                .build();
        List<MultipartFile> fileList = new ArrayList<>();
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            fileList.add(dto.getFile());
        }

        CvEvaluationResponse response = ApiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.CV_ANALYSIS,
                cvEvaluationRequest,
                "java-backend",
                false,
                fileList,
                CvEvaluationResponse.class);
        Map<String, String> map = cloudinaryService.uploadDocument(dto.getFile());
        String cvUrl = map.get("secure_url");
        ApplicationDetail applicationDetail = new ApplicationDetail();
        applicationDetail.setApplicationId(dto.getApplication().getId());
        applicationDetail.setRoundId(dto.getRound().getId());
        applicationDetail.setStatus(ApplicationDetailStatus.AI_EVALUATED);
        ApplicationDetail.SubmissionData submissionData =
                ApplicationDetail.SubmissionData.builder().fileUrl(cvUrl).build();
        applicationDetail.setSubmissionData(submissionData);
        applicationDetail.setAiScore(response.getScore());
        applicationDetail.setAiFeedback(parseRawMetrics(response.getExtraMetrics()));
        applicationDetailRepository.save(applicationDetail);
    }

    public static ApplicationDetail.AiFeedback parseRawMetrics(Map<String, Object> rawMap) {
        if (rawMap == null) {
            return null;
        }

        ApplicationDetail.AiFeedback feedback = new ApplicationDetail.AiFeedback();
        Map<String, Object> extraMetricsMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "General Comment":
                    feedback.setGeneralComment(value != null ? value.toString() : "");
                    break;

                case "Strength":
                    feedback.setStrengths(castToListString(value));
                    break;

                case "Weakness":
                    feedback.setWeaknesses(castToListString(value));
                    break;
                default:
                    extraMetricsMap.put(key, value);
                    break;
            }
        }

        feedback.setExtraMetrics(extraMetricsMap);
        return feedback;
    }

    /**
     * Hàm hỗ trợ ép kiểu dữ liệu Object sang List<String> an toàn, tránh lỗi
     * ClassCastException
     */
    @SuppressWarnings("unchecked")
    private static List<String> castToListString(Object obj) {
        if (obj instanceof List) {
            return (List<String>) obj;
        }
        List<String> list = new ArrayList<>();
        if (obj != null) {
            list.add(obj.toString());
        }
        return list;
    }
}
