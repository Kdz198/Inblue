package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

@Service
public class MentorReviewSummaryBuilder implements RoundSummaryBuilder {
    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        MentorReview review = detail.getMentorReview();

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.MENTROR_REVIEW)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getFinalScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(
                        detail.getFinalResult() != null
                                ? detail.getFinalResult().name()
                                : null)
                .summary(buildSummary(review))
                .strengths(review != null && review.getStrength() != null ? List.of(review.getStrength()) : List.of())
                .weaknesses(review != null && review.getWeakness() != null ? List.of(review.getWeakness()) : List.of())
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

    private String buildSummary(MentorReview review) {
        if (review == null) {
            return "Mentor review has not been recorded.";
        }

        return joinNonBlank(
                " | ",
                "Rating: " + review.getRating() + "/10",
                review.getSituationNote() != null ? "Situation: " + review.getSituationNote() : null,
                review.getTaskNote() != null ? "Task: " + review.getTaskNote() : null,
                review.getActionNote() != null ? "Action: " + review.getActionNote() : null,
                review.getResultNote() != null ? "Result: " + review.getResultNote() : null,
                review.getImprove() != null ? "Improve: " + review.getImprove() : null);
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
