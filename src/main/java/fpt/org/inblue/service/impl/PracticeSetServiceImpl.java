package fpt.org.inblue.service.impl;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PracticeAIRequest;
import fpt.org.inblue.model.dto.request.PracticeGenerateRequest;
import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import fpt.org.inblue.model.dto.request.PracticeRequest;
import fpt.org.inblue.model.dto.response.PracticeSetAIResponse;
import fpt.org.inblue.model.dto.response.PracticeSetResponse;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.model.enums.TargetLevel;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.PracticeSetItemRepository;
import fpt.org.inblue.repository.PracticeSetRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PracticeSetServiceImpl implements PracticeSetService {
    @Autowired
    private PracticeSetRepository practiceSetRepository;
    @Autowired
    private MajorService majorService;
    @Autowired
    private QuestionLessonService questionLessonService;
    @Autowired
    private PracticeQuestionService practiceQuestionService;
    @Autowired
    private PracticeSetItemRepository practiceSetItemRepository;
    @Autowired
    private PythonApiClient pythonApiClient;
    @Autowired
    private InterviewSessionRepository interviewSessionRepository;
    @Autowired
    @Lazy
    private PracticeSetService practiceSetService;
    @Autowired
    private UserRepository userRepository;


    @Override
    public PracticeSet getQuestionSet(int id) {
        return practiceSetRepository.findById(id);
    }

    @Override
    public PracticeSet createQuestionSet(PracticeSet practiceSet) {
        return practiceSetRepository.save(practiceSet);
    }

    @Override
    public PracticeSet updateQuestionSet(PracticeSet practiceSet) {
        if(practiceSetRepository.existsById(practiceSet.getId())){
            return practiceSetRepository.save(practiceSet);
        }
        else{
            throw new CustomException("question set not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionSet(int id) {
        if(practiceSetRepository.existsById(id)){
            practiceSetRepository.deleteById(id);
        }
        else{
            throw new CustomException("question set not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<PracticeSet> getAllQuestionSets() {
        return practiceSetRepository.findAll();
    }

    @Override
    public List<PracticeSet> getQuestionSetsByTargetLevel(String level) {
        return practiceSetRepository.findAllByLevel(TargetLevel.valueOf(level));
    }

    /**
     * Tao bo on tap hoan chinh
     * Một hàm chỉ tạo 1 bộ câu hỏi thôi nếu mà user yêu cầu tạo bao nhiêu bộ thì gọi hàm này bấy nhiêu lần
     * @param practiceRequest
     * @return
     */
    @Override
    @Transactional
    public PracticeSet createFullSet(PracticeRequest practiceRequest) {
        Major major = majorService.getMajorById(practiceRequest.getMajorId());
        PracticeSet practiceSet = PracticeSet.builder()
                .practiceSetName(practiceRequest.getPracticeSetName())
                .objective(practiceRequest.getObjective())
                .level(practiceRequest.getTarget())
                .major(major)
                .startDate(Date.valueOf(LocalDate.now().plusDays(practiceRequest.getDateNumber()))) //ngay bat dau on tap la ngay hien tai + số ngày mà AI đề xuất
                .build();
        practiceSet = practiceSetRepository.save(practiceSet);

        //luu cac cau hoi on tap
        for(int i = 0 ; i < practiceRequest.getQuestions().size(); i++){
            PracticeQuestionRequest question = practiceRequest.getQuestions().get(i);
            //lay lesson tuong ung nếu chưa có thì tạo mới rồi thêm
            QuestionLesson lesson = questionLessonService.findByName(question.getLessonName());
            if(lesson==null){
                lesson = questionLessonService.createQuestionLesson(
                        QuestionLesson.builder()
                        .lessonName(question.getLessonName())
                        .build());
            }

            PracticeQuestion saved = PracticeQuestion.builder()
                    .title(question.getTitle())
                    .content(question.getContent())
                    .level(question.getLevel())
                    .hint(question.getHint())
                    .answer(question.getAnswer())
                    .lesson(lesson)
                    .build();
            practiceQuestionService.createQuestion(saved);

            PracticeSetItem item = PracticeSetItem.builder()
                    .practiceQuestion(saved)
                    .practiceSet(practiceSet)
                    .orderIndex(i+1)
                    .build();
            practiceSetItemRepository.save(item);
        }
        return practiceSet;
    }


    @Override
    public List<PracticeSetAIResponse>  creatPracticeSetByAI(PracticeGenerateRequest request) {
        PracticeAIRequest aiRequest = new PracticeAIRequest();
        InterviewSession interviewSession = interviewSessionRepository.findById(request.getAiInterviewId()).orElse(null);
        CandidateProfile candidateProfile = interviewSession.getCandidateProfile();
        aiRequest.setQaResults(interviewSession.getResultDetail().getHistory());
        aiRequest.setTargetLevel(candidateProfile.getTargetLevel());
        aiRequest.setTargetRole(candidateProfile.getTargetRole());
        aiRequest.setCandidateIntroduction(candidateProfile.getIntroduction());
        aiRequest.setPracticeSetRequest(request.getDateNumber());
        List<PracticeSetAIResponse> response = callPython(aiRequest);
        for(PracticeSetAIResponse aiResponse : response){
            PracticeRequest practiceRequest = new PracticeRequest();
            practiceRequest.setPracticeSetName(aiResponse.getPracticeSetName());
            practiceRequest.setObjective(aiResponse.getObjective());
            practiceRequest.setTarget(TargetLevel.convertFromStringToEnum(candidateProfile.getTargetLevel()));
//            practiceRequest.setMajorId(request.getMajorId());
            System.out.println("date number from python: " + aiResponse.getDateNumber());
            practiceRequest.setDateNumber(aiResponse.getDateNumber());
            practiceRequest.setQuestions(aiResponse.getQuestions());
            practiceRequest.setUserId(request.getUserId());
            practiceSetService.createFullSetByAI(practiceRequest, request.getAiInterviewId());
        }
        return response;

    }

    @Transactional
    @Override
    public void createFullSetByAI(PracticeRequest practiceRequest,int aiInterviewId) {
        Major major = majorService.getMajorById(1);
        System.out.println("DAte number: " + practiceRequest.getDateNumber());
        System.out.println(Date.valueOf(LocalDate.now().plusDays(practiceRequest.getDateNumber())));
        PracticeSet practiceSet = PracticeSet.builder()
                .practiceSetName(practiceRequest.getPracticeSetName())
                .objective(practiceRequest.getObjective())
                .level(practiceRequest.getTarget())
                .major(major)
                .startDate(Date.valueOf(LocalDate.now().plusDays(practiceRequest.getDateNumber()+1)))
                .build();
        practiceSet = practiceSetRepository.save(practiceSet);

        //list này để thêm vào cột trong practice set để fe lấy lên cho dễ
        List<PracticeQuestion> questions= new ArrayList<>();
        //luu cac cau hoi on tap
        for(int i = 0 ; i < practiceRequest.getQuestions().size(); i++){
            PracticeQuestionRequest question = practiceRequest.getQuestions().get(i);
            //lay lesson tuong ung nếu chưa có thì tạo mới rồi thêm
            QuestionLesson lesson = questionLessonService.findByName(question.getLessonName());
            if(lesson==null){
                lesson = questionLessonService.createQuestionLesson(
                        QuestionLesson.builder()
                                .lessonName(question.getLessonName())
                                .build());
            }

            PracticeQuestion saved = PracticeQuestion.builder()
                    .title(question.getTitle())
                    .content(question.getContent())
                    .level(question.getLevel())
                    .hint(question.getHint())
                    .answer(question.getAnswer())
                    .lesson(lesson)
                    .build();
            questions.add(saved);
            practiceQuestionService.createQuestion(saved);

            PracticeSetItem item = PracticeSetItem.builder()
                    .practiceQuestion(saved)
                    .practiceSet(practiceSet)
                    .orderIndex(i+1)
                    .build();
            practiceSetItemRepository.save(item);
        }
        User user = userRepository.findById(practiceRequest.getUserId()).orElse(null);
        practiceSet.setUser(user);
        practiceSet.setQuestions(questions);
        practiceSet.setInterviewSessionId(aiInterviewId);
        practiceSetRepository.save(practiceSet);
    }

    @Override
    public List<PracticeSet> getAllByInterviewSession(int interviewSessionId) {
        return practiceSetRepository.findAllByInterviewSessionId(interviewSessionId);
    }

    private List<PracticeSetAIResponse> callPython(PracticeAIRequest request) {
        PracticeSetAIResponse[] response = pythonApiClient.callApi(
                PythonService.LLM,
                ApiPath.GENERATE_PRACTICE_SET_API,
                HttpMethod.POST,
                request,
                PracticeSetAIResponse[].class
        );
        return List.of(response);
    }

    /**
     * Lấy bộ on tap hoàn chỉnh bao gồm cả các câu hỏi bên trong
     * @param id
     * @return
     */
    @Override
    public PracticeSetResponse getFullSet(int id) {
        PracticeSet practiceSet = getQuestionSet(id);
        if(practiceSet!=null){
            List<PracticeSetItem> item = practiceSetItemRepository.findAllByPracticeSet_Id(id);
            PracticeSetResponse response = new PracticeSetResponse();
            response.setPracticeSet(practiceSet);
            response.setPracticeSetItem(item);
            return response;
        }
        else{
            throw new CustomException("practice set not found", HttpStatus.NOT_FOUND);
        }
    }


}
