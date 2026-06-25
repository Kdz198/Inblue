package fpt.org.inblue.model.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodingProblemGenerateResponse {

    private String title;
    private String difficulty;
    private String problemStatement;
    private List<String> rulesAndConstraints;
    private List<Example> visibleExamples;
    private Integer executionTimeLimitMs;
    private Integer memoryLimitMb;
    private Map<String, String> codeStubs;
    private List<TestCase> hiddenTestCases;
    private List<String> paramTypes;
    private String returnType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Example {
        private List<String> inputs;
        private String output;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestCase {
        private List<String> inputs;
        private String expectedOutput;
        private Integer weightPoints;
    }
}
