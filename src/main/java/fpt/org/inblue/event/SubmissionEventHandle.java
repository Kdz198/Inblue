package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.constants.CvMetricsConstant;
import fpt.org.inblue.constants.EmailMetricsConstant;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.CodingProblem;
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
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.submission.SubmissionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

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

    @Async
    @EventListener
    public void handleEventSubmission(ProcessDto dto) throws IOException {
        switch (dto.getRoundType()) {
            case CV_SCREENING -> processCvSubmission(dto);
            case EMAIL_SIMULATOR -> processEmailSubmission(dto);
            default ->
                    throw new CustomException("Unsupported round type: " + dto.getRoundType(), HttpStatus.BAD_REQUEST);
        }
    }

    private void processCodeSubmission(ProcessDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu tiến trình xử lý (ProcessDto) không được trống");
        }

        CompileRequest compileRequest = dto.getCompileRequest();
        // Ta chủ động bốc chuỗi textJson từ textContent hoặc trường thô nếu có để tự parse trực tiếp tại đây.
        if (compileRequest == null && dto.getTextContent() != null) {
            try {
                compileRequest = objectMapper.readValue(dto.getTextContent(), CompileRequest.class);
                dto.setCompileRequest(compileRequest);
            } catch (Exception e) {
                log.error("Không thể giải mã dữ liệu cấu hình Compile từ textContent: {}", e.getMessage());
                throw new IllegalArgumentException("Cấu trúc JSON bài làm Coding không hợp lệ", e);
            }
        }
        if (compileRequest == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin yêu cầu compile bài toán (CompileRequest là null)");
        }
        CodingProblem problem = codingProblemsRepository.findById(compileRequest.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài toán mã số: " + dto.getCompileRequest().getProblemId()));

        //  Nạp toàn bộ các Test Case
        List<CompilerRequestDto.TestCase> testcases = new ArrayList<>();
        if (problem.getVisibleExamples() != null) {
            for (CodingProblem.Example example : problem.getVisibleExamples()) {
                CompilerRequestDto.TestCase testCase = CompilerRequestDto.TestCase.builder()
                        .expectedOutput(example.getOutput())
                        .inputs(example.getInputs())
                        .build();
                testcases.add(testCase);
            }
        }

        // Nạp toàn bộ các Test Case Ẩn (Hidden Test Cases) để chấm điểm tuyệt đối
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
        // Gọi gọi Client Service thực thi code, nhận kết quả trả về từ Sandbox
        CompilerResponseDto response = apiClient.executeCode(requestDto);
        //PENDING.............

        // Xử lý nộp bài chính thức tại đây nếu test == false (ví dụ lưu kết quả vào DB và tính điểm)
    }

    private void processEmailSubmission(ProcessDto dto) {
        Round round = dto.getRound();
        Optional<JobDescription> jobDescription = jobDescriptionRepository.findById(dto.getApplication().getJdId());
        if (jobDescription.isEmpty()) {
            throw new CustomException("Job Description not found for id: " + dto.getApplication().getJdId(), HttpStatus.NOT_FOUND);
        }
        List<String> criteria = new ArrayList<>(List.of(EmailMetricsConstant.CLOSING_AND_SIGNATURE, EmailMetricsConstant.FORMATTING_AND_STRUCTURE, EmailMetricsConstant.CONTENT_AND_CLARITY, EmailMetricsConstant.GRAMMAR_AND_VOCABULARY, EmailMetricsConstant.GENERAL_COMMENT, EmailMetricsConstant.SALUTATION_AND_OPENING, EmailMetricsConstant.STRENGTH, EmailMetricsConstant.SUBJECT_LINE_QUALITY, EmailMetricsConstant.TONE_AND_PROFESSIONALISM, EmailMetricsConstant.WEAKNESS));
        EmailEvaluationRequest.EvaluationCriteria evaluation = EmailEvaluationRequest.EvaluationCriteria.builder()
                .maxScore(round.getConfigData().getMaxScore())
                .aiSystemPrompt(round.getConfigData().getAiSystemPrompt())
                .extraMetrics(criteria)
                .build();
        EmailEvaluationRequest.EmailContext context = EmailEvaluationRequest.EmailContext.builder()
                .scenario(round.getConfigData().getEvaluationCriteria())
                .level(String.valueOf(jobDescription.get().getLevel()))
                .candidateEmail(dto.getTextContent())
                .build();
        EmailEvaluationRequest emailEvaluationRequest = EmailEvaluationRequest.builder()
                .emailContext(context)
                .evaluationCriteria(evaluation)
                .build();
        //Gọi LLM API để chấm điểm email
        CvEvaluationResponse response = ApiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.EMAIL,
                emailEvaluationRequest,
                "java-backend",
                false,
                null,
                CvEvaluationResponse.class
        );

        ApplicationDetail applicationDetail = new ApplicationDetail();
        applicationDetail.setApplicationId(dto.getApplication().getId());
        applicationDetail.setRoundId(dto.getRound().getId());
        applicationDetail.setStatus(ApplicationDetailStatus.AI_EVALUATED);
        ApplicationDetail.SubmissionData submissionData = ApplicationDetail.SubmissionData.builder()
                .textContent(dto.getTextContent())
                .build();
        applicationDetail.setSubmissionData(submissionData);
        applicationDetail.setAiScore(response.getScore());
        applicationDetail.setAiFeedback(parseRawMetrics(response.getExtraMetrics()));
        applicationDetailRepository.save(applicationDetail);
    }

    private void processCvSubmission(ProcessDto dto) throws IOException {
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            System.err.println("File not found");
        }
        Round round = dto.getRound();
        Optional<JobDescription> jobDescription = jobDescriptionRepository.findById(dto.getApplication().getJdId());
        if (jobDescription.isEmpty()) {
            throw new CustomException("Job Description not found for id: " + dto.getApplication().getJdId(), HttpStatus.NOT_FOUND);
        }
        List<String> criteria = new ArrayList<>(List.of(CvMetricsConstant.CV_READABILITY_SCORE, CvMetricsConstant.EDUCATION_MATCH_SCORE, CvMetricsConstant.EXPERIENCE_MATCH_SCORE, CvMetricsConstant.KEYWORD_DENSITY, CvMetricsConstant.OVERALL_CV_MATCH, CvMetricsConstant.POTENTIAL_RED_FLAGS, CvMetricsConstant.SKILLS_MATCH_SCORE, CvMetricsConstant.STRENGTH, CvMetricsConstant.WEAKNESS, CvMetricsConstant.GENERAL_COMMENT));
        CvEvaluationRequest.EvaluationCriteria evaluation = CvEvaluationRequest.EvaluationCriteria.builder()
                .maxScore(round.getConfigData().getMaxScore())
                .aiSystemPrompt(round.getConfigData().getAiSystemPrompt())
                .extraMetrics(criteria)
                .build();
        CvEvaluationRequest.JD jd = CvEvaluationRequest.JD.builder()
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
                CvEvaluationResponse.class
        );
        Map<String, String> map = cloudinaryService.uploadDocument(dto.getFile());
        String cvUrl = map.get("secure_url");
        ApplicationDetail applicationDetail = new ApplicationDetail();
        applicationDetail.setApplicationId(dto.getApplication().getId());
        applicationDetail.setRoundId(dto.getRound().getId());
        applicationDetail.setStatus(ApplicationDetailStatus.AI_EVALUATED);
        ApplicationDetail.SubmissionData submissionData = ApplicationDetail.SubmissionData.builder()
                .fileUrl(cvUrl)
                .build();
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
     * Hàm hỗ trợ ép kiểu dữ liệu Object sang List<String> an toàn, tránh lỗi ClassCastException
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
