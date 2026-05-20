package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.JobDescriptionService;
import fpt.org.inblue.service.RoundService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoundServiceImpl implements RoundService {
    private final RoundRepository roundRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    public RoundServiceImpl(RoundRepository roundRepository, JobDescriptionRepository jobDescriptionRepository) {
        this.roundRepository = roundRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    @Override
    public List<Round> setUpRoundForJd(Long jdId, SetupJdRoundsRequest request) {
        Optional<JobDescription> jd = jobDescriptionRepository.findById(jdId);
        if(jd.isEmpty()){
            throw new CustomException("Job Description không tồn tại", HttpStatus.NOT_FOUND);
        }
        List<Round> rounds = new ArrayList<>();
        for(SetupJdRoundsRequest.RoundItemDto item : request.getRounds()){
            Round round = new Round();
            round.setName(item.getName());
            round.setRoundOrder(item.getRoundOrder());
            round.setRoundType(item.getRoundType());
            round.setPassThreshold(item.getPassThreshold());
            Round.RoundConfig roundConfig = new Round.RoundConfig();
            roundConfig.setInstruction(item.getConfigData().getInstruction());
            roundConfig.setSubmissionFormat(item.getConfigData().getSubmissionFormat());
            roundConfig.setTimeLimitMinutes(item.getConfigData().getTimeLimitMinutes());
            roundConfig.setMaxScore(item.getConfigData().getMaxScore());
            roundConfig.setAiSystemPrompt(item.getConfigData().getAiSystemPrompt());
            roundConfig.setEvaluationCriteria(item.getConfigData().getEvaluationCriteria());
            List<Round.QuizQuestion> quizQuestions = new ArrayList<>();
            if(item.getConfigData().getQuizQuestions() != null){
                for(int i = 0; i < item.getConfigData().getQuizQuestions().size(); i++){
                    Round.QuizQuestion question = new Round.QuizQuestion();
                    SetupJdRoundsRequest.QuizQuestionDto questionDto = item.getConfigData().getQuizQuestions().get(i);
                    question.setQuestionText(questionDto.getQuestionText());
                    question.setOptions(questionDto.getOptions());
                    question.setCorrectAnswer(questionDto.getCorrectAnswer());
                    question.setPoints(questionDto.getPoints());
                    quizQuestions.add(question);
                }
            }
            roundConfig.setQuizQuestions(quizQuestions);
            round.setConfigData(roundConfig);
            rounds.add(round);
            jd.get().getRounds().add(round);
        }


        jobDescriptionRepository.save(jd.get());

        return rounds;
    }


}
