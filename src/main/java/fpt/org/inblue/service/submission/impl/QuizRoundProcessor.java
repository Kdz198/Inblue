package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.exception.GlobalExceptionHandler;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.SubmissionResult;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static fpt.org.inblue.enums.RoundType.QUIZ;

@Component
public class QuizRoundProcessor implements RoundSubmissionProcessor {
    private final RoundRepository roundRepository;
    private final ApplicationDetailRepository applicationDetailRepository;

    public QuizRoundProcessor(RoundRepository roundRepository, ApplicationDetailRepository applicationDetailRepository) {
        this.roundRepository = roundRepository;
        this.applicationDetailRepository = applicationDetailRepository;
    }

    @Override
    public RoundType getSupportedType() {
        return QUIZ;
    }

    @Override
    @Transactional
    public SubmissionResult process(ProcessDto dto) {
        //Lấy dữ liệu từ câu hỏi ra và đối chiếu với đáp án của user
        Round round = dto.getRound();
        ApplicationDetail applicationDetail = new ApplicationDetail();
        List<ApplicationDetail.QuizAnswer> quizAnswers = new ArrayList<>();
        double score = 0.0;
        for (int i = 0; i < round.getConfigData().getQuizQuestions().size(); i++) {
            String correctAnswer = round.getConfigData().getQuizQuestions().get(i).getCorrectAnswer();
            String userAnswer = dto.getQuizAnswers().get(i);
            quizAnswers.add(new ApplicationDetail.QuizAnswer(round.getConfigData().getQuizQuestions().get(i).getQuestionText(), userAnswer, correctAnswer.equals(userAnswer)));
            score+= correctAnswer.equals(userAnswer) ? round.getConfigData().getQuizQuestions().get(i).getPoints() : 0.0;
        }
        applicationDetail.setSubmissionData(ApplicationDetail.SubmissionData
                .builder()
                .quizAnswers(quizAnswers)
                .build());
        applicationDetail.setApplicationId(dto.getApplication().getId());
        applicationDetail.setRoundId(dto.getRound().getId());
        applicationDetail.setFinalScore(score);
        applicationDetail.setFinalResult(score>= round.getPassThreshold() && round.getIsAuto()? ApplicationDetail.RoundResult.PASSED : ApplicationDetail.RoundResult.FAILED);
        applicationDetailRepository.save(applicationDetail);
        return SubmissionResult.completed(applicationDetail);
    }
}
