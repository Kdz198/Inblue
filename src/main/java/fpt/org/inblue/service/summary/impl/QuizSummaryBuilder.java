package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QuizSummaryBuilder implements RoundSummaryBuilder {
    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        List<ApplicationDetail.QuizAnswer> quizAnswers = detail.getSubmissionData() != null
                        && detail.getSubmissionData().getQuizAnswers() != null
                ? detail.getSubmissionData().getQuizAnswers()
                : Collections.emptyList();

        long correctCount = quizAnswers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        String wrongTopics = quizAnswers.stream()
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .map(ApplicationDetail.QuizAnswer::getQuestionText)
                .collect(Collectors.joining(", "));

        String summary = wrongTopics.isBlank()
                ? String.format("Correct: %d/%d. No wrong topics.", correctCount, quizAnswers.size())
                : String.format("Correct: %d/%d. Topics wrong: %s", correctCount, quizAnswers.size(), wrongTopics);

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.QUIZ)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getFinalScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(detail.getFinalResult() != null ? detail.getFinalResult().name() : null)
                .summary(summary)
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
}
