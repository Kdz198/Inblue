package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.CompilerLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilerRequestDto {
    private CompilerLanguage language;
    private List<String> sourceCode;
    private int timeLimitMs;
    private int memoryLimitMb;
    private List<TestCase> testCases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private String input;
        private String expectedOutput;
    }
}