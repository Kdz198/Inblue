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
import org.springframework.stereotype.Service;

@Service
public class CvSummaryBuilder implements RoundSummaryBuilder {
    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        ApplicationDetail.AiFeedback feedback = detail.getAiFeedback();

        String generalComment = feedback != null ? feedback.getGeneralComment() : null;
        String extraMetrics = describeExtraMetrics(feedback);
        List<String> strengths =
                feedback != null && feedback.getStrengths() != null ? feedback.getStrengths() : Collections.emptyList();
        List<String> weaknesses = feedback != null && feedback.getWeaknesses() != null
                ? feedback.getWeaknesses()
                : Collections.emptyList();

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.CV_SCREENING)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getAiScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(
                        detail.getFinalResult() != null
                                ? detail.getFinalResult().name()
                                : null)
                .summary(joinNonBlank(" | ", generalComment, extraMetrics, fallbackSummary(feedback)))
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

    private String describeExtraMetrics(ApplicationDetail.AiFeedback feedback) {
        if (feedback == null
                || feedback.getExtraMetrics() == null
                || feedback.getExtraMetrics().isEmpty()) {
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
