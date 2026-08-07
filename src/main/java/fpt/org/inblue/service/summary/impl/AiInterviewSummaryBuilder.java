package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.InterviewResultDetail;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

@Service
public class AiInterviewSummaryBuilder implements RoundSummaryBuilder {
    private final InterviewSessionRepository interviewSessionRepository;

    public AiInterviewSummaryBuilder(InterviewSessionRepository interviewSessionRepository) {
        this.interviewSessionRepository = interviewSessionRepository;
    }

    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        ApplicationDetail.AiFeedback feedback = detail.getAiFeedback();
        Optional<InterviewSession> session = findInterviewSession(detail);

        String sessionSummary = session.map(this::buildSessionSummary).orElse(null);
        String generalComment = feedback != null ? feedback.getGeneralComment() : null;
        String extraMetrics = describeExtraMetrics(feedback);
        List<String> strengths =
                feedback != null && feedback.getStrengths() != null ? feedback.getStrengths() : Collections.emptyList();
        List<String> weaknesses = feedback != null && feedback.getWeaknesses() != null
                ? feedback.getWeaknesses()
                : Collections.emptyList();

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.AI_INTERVIEW)
                .roundOrder(roundConfig.getRoundOrder())
                .score(session.map(InterviewSession::getOverallScore)
                        .orElse(detail.getFinalScore() != null ? detail.getFinalScore() : detail.getAiScore()))
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(resolveFinalResult(detail, session))
                .summary(joinNonBlank(" | ", sessionSummary, generalComment, extraMetrics, fallbackSummary(feedback)))
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
        return feedback == null ? "Chua co du lieu AI Interview cho vong nay" : null;
    }

    private Optional<InterviewSession> findInterviewSession(ApplicationDetail detail) {
        if (detail.getAiInterviewSessionId() == null) {
            return Optional.empty();
        }
        return interviewSessionRepository.findById(detail.getAiInterviewSessionId());
    }

    private String buildSessionSummary(InterviewSession session) {
        InterviewResultDetail resultDetail = session.getResultDetail();
        String overview = resultDetail != null ? resultDetail.getAiOverviewFeedback() : null;
        String questionSummary = resultDetail != null && resultDetail.getHistory() != null
                ? "Answered questions: " + resultDetail.getHistory().size()
                : null;
        String result = session.getResult() != null
                ? "Evaluation: " + session.getResult().name()
                : null;

        return joinNonBlank(" | ", result, questionSummary, overview);
    }

    private String resolveFinalResult(ApplicationDetail detail, Optional<InterviewSession> session) {
        if (detail.getFinalResult() != null) {
            return detail.getFinalResult().name();
        }
        return session.map(InterviewSession::getResult).map(Enum::name).orElse(null);
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
