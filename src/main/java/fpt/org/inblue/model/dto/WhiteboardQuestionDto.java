package fpt.org.inblue.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhiteboardQuestionDto {

    @JsonProperty("title")
    private String title;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("problem_statement")
    private String problemStatement;

    @JsonProperty("rules_and_constraints")
    private List<String> rulesAndConstraints;

    @JsonProperty("examples")
    private List<Example> examples;

    @JsonProperty("suggested_grading_rubric")
    private String suggestedGradingRubric;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Example {

        @JsonProperty("input")
        private String input;

        @JsonProperty("output")
        private String output;

        @JsonProperty("explanation")
        private String explanation;
    }
}