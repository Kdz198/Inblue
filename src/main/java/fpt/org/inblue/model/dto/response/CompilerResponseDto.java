package fpt.org.inblue.model.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class CompilerResponseDto {
    private String status;
    private int passedTestCases;
    private int totalTestCases;
    private long executionTimeMs;
    private String errorMessage;
    private List<TestCaseResult> testCases;

    @Data
    public static class TestCaseResult {
        private int index;
        private String status;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private long executionTimeMs;
        private String errorMessage;
    }
}
