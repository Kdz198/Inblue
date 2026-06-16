package fpt.org.inblue.service.submission.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.request.CompileRequest;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.service.submission.SubmissionResult;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodeRoundProcessor implements RoundSubmissionProcessor {

    private final ApiClient apiClient;
    private final CodingProblemsRepository codingProblemsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public RoundType getSupportedType() {
        return RoundType.CODING;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) throws IOException {
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

        // Phân luồng xử lý: chayj code đơn lẻ
        if (compileRequest.isTest()) {
            // Gọi gọi Client Service thực thi code, nhận kết quả trả về từ Sandbox
            CompilerResponseDto response = apiClient.executeCode(requestDto);
            return SubmissionResult.compileCode(response);
        } else {
             applicationEventPublisher.publishEvent(dto);
             return SubmissionResult.pending(dto.getApplication().getId());
        }

        // Xử lý nộp bài chính thức tại đây nếu test == false (ví dụ lưu kết quả vào DB và tính điểm)
    }
}