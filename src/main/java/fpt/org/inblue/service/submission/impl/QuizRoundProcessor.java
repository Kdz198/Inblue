package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.exception.GlobalExceptionHandler;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static fpt.org.inblue.enums.RoundType.QUIZ;

@Component
public class QuizRoundProcessor implements RoundSubmissionProcessor {
    private final RoundRepository roundRepository;

    public QuizRoundProcessor(RoundRepository roundRepository, ApplicationDetailRepository applicationDetailRepository) {
        this.roundRepository = roundRepository;}

    @Override
    public RoundType getSupportedType() {
        return QUIZ;
    }

    @Override
    public ApplicationDetail process(SubmitRequest detail) {
        //Lấy dữ liệu từ câu hỏi ra và đối chiếu với đáp án của user
        Round round = roundRepository.findById(detail.getRoundId())
                .orElseThrow(() -> new CustomException("Không tìm thấy vòng thi", HttpStatus.NOT_FOUND));
        ApplicationDetail applicationDetail = new ApplicationDetail();
        List<ApplicationDetail.QuizAnswer> quizAnswers = new ArrayList<>();
        double score = 0.0;
        for (int i = 0; i < round.getConfigData().getQuizQuestions().size(); i++) {
            String correctAnswer = round.getConfigData().getQuizQuestions().get(i).getCorrectAnswer();
            String userAnswer = detail.getSubmissionData().getQuizAnswers().get(i);
            quizAnswers.add(new ApplicationDetail.QuizAnswer(round.getConfigData().getQuizQuestions().get(i).getQuestionText(), userAnswer, correctAnswer.equals(userAnswer)));
            score+= correctAnswer.equals(userAnswer) ? round.getConfigData().getQuizQuestions().get(i).getPoints() : 0.0;
        }
        applicationDetail.setSubmissionData(new ApplicationDetail.SubmissionData(null,null,quizAnswers));
        applicationDetail.setApplicationId(detail.getApplicationId());
        applicationDetail.setRoundId(detail.getRoundId());
        applicationDetail.setFinalScore(score);
        applicationDetail.setFinalResult(score>= round.getPassThreshold()? ApplicationDetail.RoundResult.PASSED : ApplicationDetail.RoundResult.FAILED);
        //Lưu điểm số và kết quả vào database
        return applicationDetail;

    }
}
