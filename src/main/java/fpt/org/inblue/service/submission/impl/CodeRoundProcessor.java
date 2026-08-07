package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.event.SubmissionEventHandle;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.request.CompileRequest;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.service.submission.SubmissionResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodeRoundProcessor implements RoundSubmissionProcessor {

    private final ApiClient apiClient;
    private final CodingProblemsRepository codingProblemsRepository;
    private final SubmissionEventHandle submissionEventHandle;

    @Override
    public RoundType getSupportedType() {
        return RoundType.CODING;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) throws IOException {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu tiến trình xử lý (ProcessDto) không được trống");
        }

        List<CompileRequest> compileRequests = dto.getCompileRequest();
        if (compileRequests == null || compileRequests.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy thông tin yêu cầu compile bài toán (CompileRequest là null hoặc rỗng)");
        }

        // Kiểm tra xem có yêu cầu nào là chạy thử (isTest == true) hay không
        CompileRequest testRequest = null;
        for (CompileRequest req : compileRequests) {

            if (Boolean.TRUE.equals(req.getIsTest())) {
                System.out.println(
                        "Phát hiện yêu cầu chạy thử (isTest == true) cho bài toán mã số: " + req.getProblemId());
                testRequest = req;
                break;
            }
        }

        if (testRequest != null) {
            System.out.println(
                    "Đang xử lý yêu cầu chạy thử (isTest == true) cho bài toán mã số: " + testRequest.getProblemId());
            final CompileRequest finalTestRequest = testRequest;
            CodingProblem problem = codingProblemsRepository
                    .findById(finalTestRequest.getProblemId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy bài toán mã số: " + finalTestRequest.getProblemId()));

            // Nạp toàn bộ các Test Case
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

            // Đóng gói Payload gửi sang hệ thống Sandbox Compile Code
            CompilerRequestDto requestDto = CompilerRequestDto.builder()
                    .language(testRequest.getLanguage())
                    .memoryLimitMb(problem.getMemoryLimitMb())
                    .sourceCode(testRequest.getSourceCode())
                    .testCases(testcases)
                    .timeLimitMs(problem.getExecutionTimeLimitMs())
                    .paramTypes(problem.getParamTypes())
                    .returnType(problem.getReturnType())
                    .build();

            // Gọi gọi Client Service thực thi code, nhận kết quả trả về từ Sandbox
            CompilerResponseDto response = apiClient.executeCode(requestDto);
            return SubmissionResult.compileCode(response);
        } else {
            return SubmissionResult.completed(submissionEventHandle.processCodeSubmission(dto));
        }

        // Xử lý nộp bài chính thức tại đây nếu test == false (ví dụ lưu kết quả vào DB
        // và tính điểm)
    }
}
