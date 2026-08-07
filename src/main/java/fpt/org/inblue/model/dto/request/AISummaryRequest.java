package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.RoundType;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISummaryRequest {
    private JobDescriptionDto jobDescription;
    private List<RoundSummaryInfo> roundSummaries;

    public String toPromptText() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("JOB DESCRIPTION\n");
        if (jobDescription != null) {
            prompt.append("- Title: ")
                    .append(valueOrEmpty(jobDescription.getTitle()))
                    .append("\n");
            prompt.append("- Level: ")
                    .append(valueOrEmpty(jobDescription.getLevel()))
                    .append("\n");
            prompt.append("- Key requirements: ")
                    .append(joinList(jobDescription.getKeyRequirements()))
                    .append("\n");
        }

        prompt.append("\nROUND SUMMARIES\n");
        List<RoundSummaryInfo> summaries = roundSummaries != null ? roundSummaries : Collections.emptyList();
        for (RoundSummaryInfo summary : summaries) {
            prompt.append("- Round: ")
                    .append(valueOrEmpty(summary.getRoundName()))
                    .append("\n");
            prompt.append("  Type: ").append(summary.getRoundType()).append("\n");
            prompt.append("  Order: ")
                    .append(valueOrEmpty(summary.getRoundOrder()))
                    .append("\n");
            prompt.append("  Score: ")
                    .append(valueOrEmpty(summary.getScore()))
                    .append("/")
                    .append(valueOrEmpty(summary.getMaxScore()))
                    .append("\n");
            prompt.append("  Final result: ")
                    .append(valueOrEmpty(summary.getFinalResult()))
                    .append("\n");
            prompt.append("  Summary: ")
                    .append(valueOrEmpty(summary.getSummary()))
                    .append("\n");
            prompt.append("  Strengths: ")
                    .append(joinList(summary.getStrengths()))
                    .append("\n");
            prompt.append("  Weaknesses: ")
                    .append(joinList(summary.getWeaknesses()))
                    .append("\n");
            prompt.append("  HR score: ")
                    .append(valueOrEmpty(summary.getHrScore()))
                    .append("\n");
            prompt.append("  HR note: ")
                    .append(valueOrEmpty(summary.getHrNote()))
                    .append("\n");
        }
        return prompt.toString();
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("; ");
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                joiner.add(value.trim());
            }
        }
        return joiner.toString();
    }

    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobDescriptionDto {
        private String title;
        private String level;
        private List<String> keyRequirements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoundSummaryInfo {
        private String roundName;
        private RoundType roundType;
        private Integer roundOrder;
        private Double score;
        private Double maxScore;
        private String finalResult;
        private String summary;
        private List<String> strengths;
        private List<String> weaknesses;
        private String hrNote;
        private Double hrScore;
    }
}
