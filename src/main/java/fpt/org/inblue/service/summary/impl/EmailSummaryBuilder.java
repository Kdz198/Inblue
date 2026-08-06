package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

@Service
public class EmailSummaryBuilder implements RoundSummaryBuilder {
    private final EmailSubmissionRepository emailSubmissionRepository;

    public EmailSummaryBuilder(EmailSubmissionRepository emailSubmissionRepository) {
        this.emailSubmissionRepository = emailSubmissionRepository;
    }

    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        ApplicationDetail.AiFeedback feedback = detail.getAiFeedback();

        String emailContext = resolveEmailContext(detail);
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
                .roundType(RoundType.EMAIL_SIMULATOR)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getAiScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(detail.getFinalResult() != null ? detail.getFinalResult().name() : null)
                .summary(joinNonBlank(" | ", emailContext, generalComment, extraMetrics, fallbackSummary(feedback)))
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

    private String resolveEmailContext(ApplicationDetail detail) {
        if (detail.getSubmissionData() == null || detail.getSubmissionData().getEmailSubmissionId() == null) {
            return null;
        }

        Long emailSubmissionId = detail.getSubmissionData().getEmailSubmissionId();
        Optional<EmailSubmission> submission = emailSubmissionRepository.findById(emailSubmissionId);

        return submission
                .map(email -> joinNonBlank(
                        ". ",
                        email.getSubject() != null ? "Subject: " + email.getSubject() : null,
                        email.getBodyText() != null ? "Body: " + shorten(email.getBodyText(), 500) : null))
                .orElse("Email submission id: " + emailSubmissionId);
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

    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
