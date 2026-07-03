package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.model.dto.request.UpdateJdRoundRequest;
import fpt.org.inblue.repository.CodeReviewProblemsRepository;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import fpt.org.inblue.service.RoundService;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoundServiceImpl implements RoundService {
    private final RoundRepository roundRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final CodingProblemsRepository codingProblemsRepository;
    private final ApplicationService applicationService;
    private final JobDescriptionService jobDescriptionService;
    private final CodeReviewProblemsRepository codeReviewProblemsRepository;

    public RoundServiceImpl(
            RoundRepository roundRepository,
            JobDescriptionRepository jobDescriptionRepository,
            CodingProblemsRepository codingProblemsRepository,
            ApplicationService applicationService,
            JobDescriptionService jobDescriptionService,
            CodeReviewProblemsRepository codeReviewProblemsRepository) {
        this.roundRepository = roundRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.codingProblemsRepository = codingProblemsRepository;
        this.applicationService = applicationService;
        this.jobDescriptionService = jobDescriptionService;
        this.codeReviewProblemsRepository = codeReviewProblemsRepository;
    }

    @Override
    public List<Round> setUpRoundForJd(Long jdId, SetupJdRoundsRequest request) {
        System.out.println("Received request to set up rounds for JD ID: " + jdId);
        Optional<JobDescription> jd = jobDescriptionRepository.findById(jdId);
        if (jd.isEmpty()) {
            throw new CustomException("Job Description không tồn tại", HttpStatus.NOT_FOUND);
        }
        List<Round> rounds = new ArrayList<>();
        for (SetupJdRoundsRequest.RoundItemDto item : request.getRounds()) {
            Round round = new Round();
            round.setName(item.getName());
            round.setRoundOrder(item.getRoundOrder());
            round.setRoundType(item.getRoundType());
            round.setPassThreshold(item.getPassThreshold());
            round.setReviewerId(item.getReviewerId());
            Round.RoundConfig roundConfig = new Round.RoundConfig();
            roundConfig.setInstruction(item.getConfigData().getInstruction());
            roundConfig.setSubmissionFormat(item.getConfigData().getSubmissionFormat());
            roundConfig.setTimeLimitMinutes(item.getConfigData().getTimeLimitMinutes());
            roundConfig.setMaxScore(item.getConfigData().getMaxScore());
            roundConfig.setAiSystemPrompt(item.getConfigData().getAiSystemPrompt());
            roundConfig.setEvaluationCriteria(item.getConfigData().getEvaluationCriteria());
            List<Round.QuizQuestion> quizQuestions = new ArrayList<>();
            if (item.getConfigData().getQuizQuestions() != null) {
                for (int i = 0; i < item.getConfigData().getQuizQuestions().size(); i++) {
                    Round.QuizQuestion question = new Round.QuizQuestion();
                    SetupJdRoundsRequest.QuizQuestionDto questionDto =
                            item.getConfigData().getQuizQuestions().get(i);
                    question.setQuestionText(questionDto.getQuestionText());
                    question.setOptions(questionDto.getOptions());
                    question.setCorrectAnswer(questionDto.getCorrectAnswer());
                    question.setPoints(questionDto.getPoints());
                    quizQuestions.add(question);
                }
                roundConfig.setQuizQuestions(quizQuestions);
            }
            if (item.getConfigData().getCodingProblemsId() != null) {
                List<Round.CodingProblemSnapshot> codingProblems = new ArrayList<>();
                for (Long codingProblemId : item.getConfigData().getCodingProblemsId()) {
                    CodingProblem cp = codingProblemsRepository
                            .findById(codingProblemId)
                            .orElseThrow(() -> new CustomException(
                                    "Coding problem không tồn tại với id: " + codingProblemId, HttpStatus.NOT_FOUND));
                    Round.CodingProblemSnapshot snapshot = Round.CodingProblemSnapshot.builder()
                            .title(cp.getTitle())
                            .codeStubs(cp.getCodeStubs())
                            .difficulty(cp.getDifficulty())
                            .executionTimeLimitMs(cp.getExecutionTimeLimitMs())
                            .memoryLimitMb(cp.getMemoryLimitMb())
                            .problemId(codingProblemId)
                            .problemStatement(cp.getProblemStatement())
                            .rulesAndConstraints(cp.getRulesAndConstraints())
                            .visibleExamples(cp.getVisibleExamples())
                            .build();

                    codingProblems.add(snapshot);
                }
                roundConfig.setCodingProblems(codingProblems);
            }
            if (item.getConfigData().getCodeReviewIds() != null) {
                List<Round.CodeReviewProblemSnapshot> codeReviewProblems = new ArrayList<>();
                for (Long codeReviewId : item.getConfigData().getCodeReviewIds()) {
                    CodeReviewProblem problem = codeReviewProblemsRepository
                            .findById(codeReviewId)
                            .orElseThrow(() -> new CustomException(
                                    "Code review problem không tồn tại với id: " + codeReviewId, HttpStatus.NOT_FOUND));
                    Round.CodeReviewProblemSnapshot snapshot = Round.CodeReviewProblemSnapshot.builder()
                            .title(problem.getTitle())
                            .files(problem.getFiles())
                            .language(problem.getLanguage())
                            .expectedIssues(problem.getExpectedIssues())
                            .problemStatement(problem.getProblemStatement())
                            .problemId(problem.getId())
                            .difficulty(problem.getDifficulty())
                            .build();
                    codeReviewProblems.add(snapshot);
                }
                roundConfig.setCodeReviewProblems(codeReviewProblems);
            }

            System.out.println("ROUND CONFIG: " + roundConfig);
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
        JobDescription jd = jobDescriptionRepository
                .findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description không tồn tại", HttpStatus.NOT_FOUND));

        Map<Long, Round> existingRoundMap =
                jd.getRounds().stream().filter(r -> r.getId() != null).collect(Collectors.toMap(Round::getId, r -> r));

        List<Round> updatedRounds = new ArrayList<>();
        for (UpdateJdRoundRequest.RoundItemDto item : request.getRounds()) {
            Round round;
            if (item.getId() != null) {
                round = existingRoundMap.get(item.getId());
                if (round == null) {
                    throw new CustomException(
                            "Round với id " + item.getId() + " không tồn tại", HttpStatus.BAD_REQUEST);
                }
            } else {
                round = new Round();
            }

            round.setName(item.getName());
            round.setRoundOrder(item.getRoundOrder());
            round.setRoundType(item.getRoundType());
            round.setPassThreshold(item.getPassThreshold());
            round.setReviewerId(item.getReviewerId());

            Round.RoundConfig roundConfig = new Round.RoundConfig();
            roundConfig.setInstruction(item.getConfigData().getInstruction());
            roundConfig.setSubmissionFormat(item.getConfigData().getSubmissionFormat());
            roundConfig.setTimeLimitMinutes(item.getConfigData().getTimeLimitMinutes());
            roundConfig.setMaxScore(item.getConfigData().getMaxScore());
            roundConfig.setAiSystemPrompt(item.getConfigData().getAiSystemPrompt());
            roundConfig.setEvaluationCriteria(item.getConfigData().getEvaluationCriteria());

            List<Round.QuizQuestion> quizQuestions = new ArrayList<>();
            if (item.getConfigData().getQuizQuestions() != null) {
                for (UpdateJdRoundRequest.QuizQuestionDto dto :
                        item.getConfigData().getQuizQuestions()) {
                    Round.QuizQuestion question = new Round.QuizQuestion();
                    question.setQuestionText(dto.getQuestionText());
                    question.setOptions(dto.getOptions());
                    question.setCorrectAnswer(dto.getCorrectAnswer());
                    question.setPoints(dto.getPoints());
                    quizQuestions.add(question);
                }
            }

            List<Round.CodingProblemSnapshot> codingProblems = new ArrayList<>();
            if (item.getConfigData().getCodingProblemsId() != null) {
                for (Long codingProblemId : item.getConfigData().getCodingProblemsId()) {
                    Optional<CodingProblem> cp = codingProblemsRepository.findById(codingProblemId);
                    if (cp.isEmpty()) {
                        throw new CustomException(
                                "Coding Problem với id " + codingProblemId + " không tồn tại", HttpStatus.BAD_REQUEST);
                    }
                    Round.CodingProblemSnapshot snapshot = Round.CodingProblemSnapshot.builder()
                            .title(cp.get().getTitle())
                            .codeStubs(cp.get().getCodeStubs())
                            .difficulty(cp.get().getDifficulty())
                            .executionTimeLimitMs(cp.get().getExecutionTimeLimitMs())
                            .memoryLimitMb(cp.get().getMemoryLimitMb())
                            .problemId(codingProblemId)
                            .problemStatement(cp.get().getProblemStatement())
                            .rulesAndConstraints(cp.get().getRulesAndConstraints())
                            .visibleExamples(cp.get().getVisibleExamples())
                            .build();
                    codingProblems.add(snapshot);
                }
            }

            List<Round.CodeReviewProblemSnapshot> codeReviewProblems = new ArrayList<>();
            if (item.getConfigData().getCodeReviewIds() != null) {
                for (Long codeReviewId : item.getConfigData().getCodeReviewIds()) {
                    CodeReviewProblem problem = codeReviewProblemsRepository
                            .findById(codeReviewId)
                            .orElseThrow(() -> new CustomException(
                                    "Code review problem với id " + codeReviewId + " không tồn tại",
                                    HttpStatus.BAD_REQUEST));
                    Round.CodeReviewProblemSnapshot snapshot = Round.CodeReviewProblemSnapshot.builder()
                            .title(problem.getTitle())
                            .files(problem.getFiles())
                            .language(problem.getLanguage())
                            .expectedIssues(problem.getExpectedIssues())
                            .problemStatement(problem.getProblemStatement())
                            .problemId(problem.getId())
                            .difficulty(problem.getDifficulty())
                            .build();
                    codeReviewProblems.add(snapshot);
                }
            }

            roundConfig.setCodingProblems(codingProblems);
            roundConfig.setQuizQuestions(quizQuestions);
            roundConfig.setCodeReviewProblems(codeReviewProblems);
            round.setConfigData(roundConfig);
            updatedRounds.add(round);
        }
        jd.getRounds().clear();
        jd.getRounds().addAll(updatedRounds);
        jobDescriptionRepository.save(jd);

        return jd.getRounds();
    }

    @Override
    public Round getRoundById(Long roundId) {
        Optional<Round> round = roundRepository.findById(roundId);
        if (round.isEmpty()) {
            throw new CustomException("Round không tồn tại", HttpStatus.NOT_FOUND);
        }
        return round.get();
    }

    @Override
    public List<RoundType> getAllRoundTypes() {
        return Arrays.asList(RoundType.values());
    }

    @Override
    public Round getRoundByOrder(Long applicationId) {
        Application currentApplication = applicationService.getApplicationById(applicationId);
        Round currentRound = jobDescriptionService.getRoundByOrder(
                currentApplication.getJdId(), currentApplication.getCurrentRoundOrder());
        return currentRound;
    }
}
