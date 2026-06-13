package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
    private List<String> pramTypes;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Example {
        private List<String> input;
        private String output;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestCase {
        private List<String> input;
        private String expectedOutput;
        private Integer weightPoints;
    }
}