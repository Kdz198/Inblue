package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.CompilerLanguage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilerRequestDto {
    private CompilerLanguage language;
    private List<String> sourceCode;
    private int timeLimitMs;
    private int memoryLimitMb;
    private List<String> paramTypes;
    private String returnType;

    private List<TestCase> testCases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private List<String> inputs;

        private String expectedOutput;
    }
}
