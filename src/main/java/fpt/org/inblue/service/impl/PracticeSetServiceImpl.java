package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import fpt.org.inblue.model.dto.request.PracticeRequest;
import fpt.org.inblue.model.dto.response.PracticeSetResponse;
import fpt.org.inblue.model.enums.TargetLevel;
import fpt.org.inblue.repository.PracticeSetItemRepository;
import fpt.org.inblue.repository.PracticeSetRepository;
import fpt.org.inblue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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


    @Override
    public PracticeSet getQuestionSet(int id) {
        return practiceSetRepository.findById(id).orElse(null);
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

    @Override
    @Transactional
    public PracticeSet createFullSet(PracticeRequest practiceRequest) {
        Major major = majorService.getMajorById(practiceRequest.getMajorId());
        PracticeSet practiceSet = PracticeSet.builder()
                .practiceSetName(practiceRequest.getPracticeSetName())
                .objective(practiceRequest.getObjective())
                .level(practiceRequest.getTarget())
                .major(major)
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
