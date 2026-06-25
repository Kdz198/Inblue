package fpt.org.inblue.model.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineCompileRequest {
    private String language;
    private List<String> sourceCode;
    // Giới hạn thời gian chạy cho mỗi test case (tính bằng mili-giây)
    private Integer timeLimitMs;
    // Giới hạn bộ nhớ cho mã nguồn khi thực thi (tính bằng MegaByte)
    private Integer memoryLimitMb;
    private List<CompilerRequestDto.TestCase> hiddenTestCases;
    private List<String> paramTypes;
    private String returnType;
}
