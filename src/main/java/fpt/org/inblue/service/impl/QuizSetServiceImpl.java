package fpt.org.inblue.service.impl;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateAIRequest;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.model.dto.response.QuizItemResponse;
import fpt.org.inblue.model.dto.response.QuizResponse;
import fpt.org.inblue.model.enums.Major;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.repository.PracticeSetRepository;
import fpt.org.inblue.repository.QuizSetRepository;
import fpt.org.inblue.service.PythonApiClient;
import fpt.org.inblue.service.QuizItemService;
import fpt.org.inblue.service.QuizSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QuizSetServiceImpl implements QuizSetService {
    @Autowired
    private PracticeSetRepository practiceSetRepository;

    @Autowired
    private QuizSetRepository quizSetRepository;

    @Autowired
    private QuizItemService quizItemService;
    @Autowired
    private PythonApiClient pythonApiClient;

    @Autowired
    @Lazy
    private QuizSetService quizSetService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public QuizSet createQuizSet(int practiceSetId, String QuizName) {
        PracticeSet practiceSet = practiceSetRepository.findById(practiceSetId);
        if(practiceSet != null){
            QuizSet quizSet = QuizSet.builder()
                    .quizName(QuizName)
                    .practiceSet(practiceSet)
                    .isSubmitted(false)
                    .build();
            return quizSetRepository.save(quizSet);
        }
        else{
            throw new CustomException("Practice Set not found", HttpStatus.NOT_FOUND);

        }
    }

    @Override
    public QuizSet getQuizById(int quizId) {
        return quizSetRepository.findById(quizId).orElse(null);
    }

    @Override
    public List<QuizSet> getHistoryByPracticeSet(int practiceSetId) {
        return quizSetRepository.findAllByPracticeSet_Id(practiceSetId);
    }

    /**
     * Nộp bài và tính điểm
     * @param quizId
     * @param userAnswers
     * @return
     */
    @Override
    public QuizSet submitAndCalculateScore(int quizId, Map<Integer, String> userAnswers) {
        QuizSet quizSet = quizSetRepository.findById(quizId)
                .orElseThrow(() -> new CustomException("Quiz không tồn tại", HttpStatus.NOT_FOUND));
        List<QuizItem> items = quizSet.getQuestions();
        int correctCount = 0;
        for (QuizItem item : items) {
            String userAnswer = userAnswers.get(item.getId());
            item.setUserResponse(userAnswer);
            if(item.getCorrectAnswer().equals(userAnswer)) {
                correctCount++;
            }
        }
        double finalScore = (double) correctCount / items.size() * 10;
        quizSet.setScore(Math.round(finalScore * 100.0) / 100.0); // Làm tròn 2 chữ số
        quizSet.setSubmitted(true);
        return quizSetRepository.save(quizSet);
    }

    @Override
    public void deleteQuizSet(int quizId) {

    }

    /**
     * Tạo bộ quiz hoàn chỉnh gồm QuizSet và các QuizItem
     * @param practiceSetId
     * @param QuizName
     * @param items
     * @return
     */
    @Override
    @Transactional
    public List<QuizItem> createFullQuizSet(int practiceSetId, String QuizName, List<QuizItemCreateRequest> items) {
        return quizItemService.saveAllItems(items);
    }

    @Override
    public QuizResponse saveAllItemsByAI(int practiceSetId){
        PracticeSet practice = practiceSetRepository.findById(practiceSetId);
        if(practice == null){
            throw new CustomException("Practice set not found", HttpStatus.NOT_FOUND);
        }

        List<QuizItemCreateAIRequest.PracticeAIQuestion> aiQuestions = new ArrayList<>();
        for(PracticeQuestion question : practice.getQuestions()){
            QuizItemCreateAIRequest.PracticeAIQuestion aiQuestion = new QuizItemCreateAIRequest.PracticeAIQuestion(question.getTitle(), question.getContent(), question.getAnswer());
            aiQuestions.add(aiQuestion);
        }
        String majorName ;
        if(practice.getMajor().equals(Major.CNTT)){
             majorName = "Công nghệ thông tin";
        }
        else{
             majorName = String.valueOf(Major.Marketing);
        }
        QuizItemCreateAIRequest request = QuizItemCreateAIRequest.builder()
                .practiceSetName(practice.getPracticeSetName())
                .level(practice.getLevel())
                .objective(practice.getObjective())
                .questions(aiQuestions)
                .majorName(majorName)
                .build();

        QuizItemCreateRequest[] response = pythonApiClient.callApi(
                PythonService.LLM,
                ApiPath.GENERATE_QUIZ_ITEM_API,
                HttpMethod.POST,
                request,
                QuizItemCreateRequest[].class
        );
        List<QuizItemCreateRequest> quizItemCreateRequests = List.of(response);
        List<QuizItem> quizItems = new ArrayList<>();

        for(QuizItemCreateRequest item : quizItemCreateRequests){
            String optionsJson = objectMapper.writeValueAsString(item.getOptions());
            QuizItem quizItem = new QuizItem();
            quizItem.setQuestion(item.getQuestion());
            quizItem.setOptions(optionsJson);
            quizItem.setCorrectAnswer(item.getCorrectAnswer());
            quizItem.setExplanation(item.getExplanation());
            quizItem.setUserResponse(null);
            quizItems.add(quizItem);
        }
        QuizSet quizSet = new QuizSet();
        quizSet.setQuizName("Bài kiểm tra: "+practice.getPracticeSetName() );
        quizSet.setPracticeSet(practice);
        quizSet.setQuestions(quizItems);
        quizSet.setScore(0);
        quizSet.setSubmitted(false);
        quizSet.setQuestions(quizItems);
        QuizSet saved = quizSetRepository.save(quizSet);
        return mapToQuizItemResponse(saved.getQuestions(), saved.getQuizId());
    }

    QuizResponse mapToQuizItemResponse(List<QuizItem> requests,int quizId)  {
        List<QuizItemResponse> responses = new ArrayList<>();
        for (QuizItem item : requests) {
            String optionsJson = objectMapper.writeValueAsString(item.getOptions());
            QuizItemResponse response = new QuizItemResponse();
            response.setId(item.getId());
            response.setQuestion(item.getQuestion());
            response.setOptions(optionsJson);
            responses.add(response);
        }
        QuizResponse quizResponse = QuizResponse.builder()
                .quizId(quizId)
                .items(responses)
                .build();
        return quizResponse;

    }
    @Override
    public List<QuizSet> getAllByPracticeSet(int practiceSetId) {
        return quizSetRepository.findAllByPracticeSet_Id(practiceSetId);
    }

    @Override
    public List<QuizSet> getAllQuizSet() {
        return quizSetRepository.findAll();
    }
}
