package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.model.dto.request.UpdateJdRoundRequest;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.RoundService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public List<Round> updateRoundForJd(Long jdId, UpdateJdRoundRequest request) {
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description không tồn tại", HttpStatus.NOT_FOUND));

        Map<Long, Round> existingRoundMap = jd.getRounds().stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(Round::getId, r -> r));

        List<Round> updatedRounds = new ArrayList<>();
        for (UpdateJdRoundRequest.RoundItemDto item : request.getRounds()) {
            Round round;
            if (item.getId() != null) {
                round = existingRoundMap.get(item.getId());
                if (round == null) {
                    throw new CustomException("Round với id " + item.getId() + " không tồn tại", HttpStatus.BAD_REQUEST);
                }
            } else {
                round = new Round();
            }

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
            if (item.getConfigData().getQuizQuestions() != null) {
                for (UpdateJdRoundRequest.QuizQuestionDto dto : item.getConfigData().getQuizQuestions()) {
                    Round.QuizQuestion question = new Round.QuizQuestion();
                    question.setQuestionText(dto.getQuestionText());
                    question.setOptions(dto.getOptions());
                    question.setCorrectAnswer(dto.getCorrectAnswer());
                    question.setPoints(dto.getPoints());
                    quizQuestions.add(question);
                }
            }
            roundConfig.setQuizQuestions(quizQuestions);
            round.setConfigData(roundConfig);
            updatedRounds.add(round);
        }
        jd.getRounds().clear();
        jd.getRounds().addAll(updatedRounds);
        jobDescriptionRepository.save(jd);

        return jd.getRounds();
    }

    @Override
    public Round getRoundById(Long roundId){
        Optional<Round> round = roundRepository.findById(roundId);
        if(round.isEmpty()){
            throw new CustomException("Round không tồn tại", HttpStatus.NOT_FOUND);
        }
        return round.get();
    }


}
