package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewSummaryBuilder implements RoundSummaryBuilder {
    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        ApplicationDetail.AiFeedback feedback = detail.getAiFeedback();

        List<ApplicationDetail.CodeReviewSubmission> submissions = detail.getSubmissionData() != null
                        && detail.getSubmissionData().getCodeReviewSubmissions() != null
                ? detail.getSubmissionData().getCodeReviewSubmissions()
                : Collections.emptyList();

        String submissionSummary = buildSubmissionSummary(submissions);
        String generalComment = feedback != null ? feedback.getGeneralComment() : null;
        String extraMetrics = describeExtraMetrics(feedback);
        List<String> strengths = feedback != null && feedback.getStrengths() != null
                ? feedback.getStrengths()
                : Collections.emptyList();
        List<String> weaknesses = feedback != null && feedback.getWeaknesses() != null
                ? feedback.getWeaknesses()
                : Collections.emptyList();

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.CODE_REVIEW)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getFinalScore() != null ? detail.getFinalScore() : detail.getAiScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(detail.getFinalResult() != null ? detail.getFinalResult().name() : null)
                .summary(joinNonBlank(" | ", submissionSummary, generalComment, extraMetrics, fallbackSummary(feedback)))
                .strengths(strengths)
                .weaknesses(weaknesses)
                .hrNote(detail.getHrNote())
                .hrScore(detail.getHrScore())
                .build();
    }

    private Double resolveMaxScore(Round roundConfig) {
        if (roundConfig.getConfigData() == null || roundConfig.getConfigData().getMaxScore() == null) {
            return null;
        }
        return roundConfig.getConfigData().getMaxScore().doubleValue();
    }

    private String fallbackSummary(ApplicationDetail.AiFeedback feedback) {
        return feedback == null ? "Chua co danh gia AI cho vong nay" : null;
    }

    private String buildSubmissionSummary(List<ApplicationDetail.CodeReviewSubmission> submissions) {
        if (submissions.isEmpty()) {
            return null;
        }

        Map<String, Long> severityCounts = submissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getSeverity() != null ? submission.getSeverity() : "UNSPECIFIED",
                        Collectors.counting()));

        String severitySummary = severityCounts.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        return "Reported issues: " + submissions.size() + " (" + severitySummary + ")";
    }

    private String describeExtraMetrics(ApplicationDetail.AiFeedback feedback) {
        if (feedback == null || feedback.getExtraMetrics() == null || feedback.getExtraMetrics().isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner("; ");
        for (Map.Entry<String, Object> entry : feedback.getExtraMetrics().entrySet()) {
            if (entry.getValue() != null) {
                joiner.add(entry.getKey() + ": " + entry.getValue());
            }
        }
        return joiner.toString();
    }

    private String joinNonBlank(String delimiter, String... values) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                joiner.add(value.trim());
            }
        }
        return joiner.toString();
    }
}
