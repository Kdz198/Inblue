package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Question;
import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.model.QuestionSetItem;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.repository.QuestionRepository;
import fpt.org.inblue.repository.QuestionSetItemRepository;
import fpt.org.inblue.service.QuestionService;
import fpt.org.inblue.service.QuestionSetItemService;
import fpt.org.inblue.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionSetItemServiceImpl implements QuestionSetItemService {
    @Autowired
    private QuestionSetItemRepository questionSetItemRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionSetService questionSetService;

    @Override
    public QuestionSetItem createQuestionSetItem(QuestionSetItem questionSetItem) {
        return questionSetItemRepository.save(questionSetItem);
    }

    @Override
    public QuestionSetItem getQuestionSetItem(int id) {
        return questionSetItemRepository.findById(id).orElseThrow(() -> new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public QuestionSetItem updateQuestionSetItem(QuestionSetItem questionSetItem) {
        if(questionSetItemRepository.existsById(questionSetItem.getQuestionSetItemId())){
            return questionSetItemRepository.save(questionSetItem);
        }
        else{
            throw new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionSetItem(int id) {
        if(questionSetItemRepository.existsById(id)){
            questionSetItemRepository.deleteById(id);
        }
        else{
            throw new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<QuestionSetItem> getAllQuestionSetItems() {
        return questionSetItemRepository.findAll();
    }

    @Override
    public List<QuestionSetItem> getQuestionSetItemsByQuestionSet(int id) {
        return questionSetItemRepository.findAllByQuestionSet_QuestionSetId(id);
    }

    /**
     * Create QuestionSetItems by selecting random questions based on difficulty levels
     * @param easy
     * @param medium
     * @param hard
     * @param questionSet
     * tạo bộ câu hỏi với số lượng câu hỏi dễ, trung bình, khó tương ứng
     * nhận về số câu rồi random từ lúc lấy db lên luôn pageable để limit lại chỉ lấy đúng số lượng cần thiết
     * @return
     */

    @Override
    public List<QuestionSetItem> createQuestionSetItems(int easy, int medium, int hard,QuestionSet questionSet) {
        List<Question> easyQuestions = questionRepository.findRandomByLevel(QuestionLevel.EASY, PageRequest.of(0,easy));
        List<Question> mediumQuestions = questionRepository.findRandomByLevel(QuestionLevel.MEDIUM, PageRequest.of(0,medium));
        List<Question> hardQuestions = questionRepository.findRandomByLevel(QuestionLevel.HARD, PageRequest.of(0,hard));

        QuestionSet saved = questionSetService.createQuestionSet(questionSet);

        List<Question> allQuestions = new ArrayList<>();
        allQuestions.addAll(easyQuestions);
        allQuestions.addAll(mediumQuestions);
        allQuestions.addAll(hardQuestions);

        List<QuestionSetItem> questionSetItems = new ArrayList<>();

        for(int i = 0; i < allQuestions.size(); i++){
            QuestionSetItem questionSetItem = new QuestionSetItem();
            questionSetItem.setQuestionSet(saved);
            questionSetItem.setQuestion(allQuestions.get(i));
            questionSetItem.setOrderIndex(i+1);
            questionSetItems.add(questionSetItem);
        }
        return questionSetItemRepository.saveAll(questionSetItems);

    }
}
