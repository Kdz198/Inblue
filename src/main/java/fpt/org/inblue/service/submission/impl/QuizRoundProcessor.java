package fpt.org.inblue.service.submission.impl;

import static fpt.org.inblue.enums.RoundType.QUIZ;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.service.submission.SubmissionResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuizRoundProcessor implements RoundSubmissionProcessor {
    private final RoundRepository roundRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationService applicationService;

    public QuizRoundProcessor(
            RoundRepository roundRepository,
            ApplicationDetailRepository applicationDetailRepository,
            ApplicationService applicationService) {
        this.roundRepository = roundRepository;
        this.applicationDetailRepository = applicationDetailRepository;
        this.applicationService = applicationService;
    }

    @Override
    public RoundType getSupportedType() {
        return QUIZ;
    }

    @Override
    @Transactional
    public SubmissionResult process(ProcessDto dto) {
        // Lấy dữ liệu từ câu hỏi ra và đối chiếu với đáp án của user
        Round round = dto.getRound();
        ApplicationDetail applicationDetail = new ApplicationDetail();
        List<ApplicationDetail.QuizAnswer> quizAnswers = new ArrayList<>();
        double earnedPoints = 0.0;
        double maxPoints = 0.0;
        for (int i = 0; i < round.getConfigData().getQuizQuestions().size(); i++) {
            String correctAnswer =
                    round.getConfigData().getQuizQuestions().get(i).getCorrectAnswer();
            String userAnswer = dto.getQuizAnswers().get(i);
            boolean isCorrect = correctAnswer.equals(userAnswer);
            quizAnswers.add(new ApplicationDetail.QuizAnswer(
                    round.getConfigData().getQuizQuestions().get(i).getQuestionText(), userAnswer, isCorrect));

            Integer pointsObj = round.getConfigData().getQuizQuestions().get(i).getPoints();
            int questionPoints = pointsObj != null ? pointsObj : 10; // Default points to 10 if null

            earnedPoints += isCorrect ? questionPoints : 0.0;
            maxPoints += questionPoints;
        }

        double scorePercentage = 0.0;
        if (maxPoints > 0) {
            scorePercentage = Math.round((earnedPoints / maxPoints) * 100.0);
        }

        applicationDetail.setSubmissionData(ApplicationDetail.SubmissionData.builder()
                .quizAnswers(quizAnswers)
                .build());
        applicationDetail.setApplicationId(dto.getApplication().getId());
        applicationDetail.setRoundId(dto.getRound().getId());
        applicationDetail.setFinalScore(scorePercentage);
        applicationDetail.setFinalResult(
                scorePercentage >= round.getPassThreshold()
                        ? ApplicationDetail.RoundResult.PASSED
                        : ApplicationDetail.RoundResult.FAILED);
        applicationDetail.setStatus(ApplicationDetailStatus.COMPLETED);
        applicationDetailRepository.save(applicationDetail);
        applicationService.moveToNextRound(dto.getApplication());
        return SubmissionResult.completed(applicationDetail);
    }
}
