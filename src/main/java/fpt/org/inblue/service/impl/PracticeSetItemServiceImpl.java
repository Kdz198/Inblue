package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.PracticeSetItem;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.repository.PracticeQuestionRepository;
import fpt.org.inblue.repository.PracticeSetItemRepository;
import fpt.org.inblue.service.PracticeQuestionService;
import fpt.org.inblue.service.PracticeSetItemService;
import fpt.org.inblue.service.PracticeSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PracticeSetItemServiceImpl implements PracticeSetItemService {
    @Autowired
    private PracticeSetItemRepository practiceSetItemRepository;
    @Autowired
    private PracticeQuestionRepository practiceQuestionRepository;
    @Autowired
    private PracticeSetService practiceSetService;

    @Override
    public PracticeSetItem createQuestionSetItem(PracticeSetItem practiceSetItem) {
        return practiceSetItemRepository.save(practiceSetItem);
    }

    @Override
    public PracticeSetItem getQuestionSetItem(int id) {
        return practiceSetItemRepository.findById(id).orElseThrow(() -> new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public PracticeSetItem updateQuestionSetItem(PracticeSetItem practiceSetItem) {
        if(practiceSetItemRepository.existsById(practiceSetItem.getId())){
            return practiceSetItemRepository.save(practiceSetItem);
        }
        else{
            throw new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionSetItem(int id) {
        if(practiceSetItemRepository.existsById(id)){
            practiceSetItemRepository.deleteById(id);
        }
        else{
            throw new CustomException("QuestionSetItem not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<PracticeSetItem> getAllQuestionSetItems() {
        return practiceSetItemRepository.findAll();
    }

    @Override
    public List<PracticeSetItem> getQuestionSetItemsByQuestionSet(int id) {
        return practiceSetItemRepository.findAllByPracticeSet_Id(id);
    }

    /**
     * Create QuestionSetItems by selecting random questions based on difficulty levels
     * @param easy
     * @param medium
     * @param hard
     * @param practiceSet
     * tạo bộ câu hỏi với số lượng câu hỏi dễ, trung bình, khó tương ứng
     * nhận về số câu rồi random từ lúc lấy db lên luôn pageable để limit lại chỉ lấy đúng số lượng cần thiết
     * @return
     */

    @Override
    public List<PracticeSetItem> createQuestionSetItems(int easy, int medium, int hard, PracticeSet practiceSet) {
        List<PracticeQuestion> easyPracticeQuestions = practiceQuestionRepository.findRandomByLevel(QuestionLevel.EASY, PageRequest.of(0,easy));
        List<PracticeQuestion> mediumPracticeQuestions = practiceQuestionRepository.findRandomByLevel(QuestionLevel.MEDIUM, PageRequest.of(0,medium));
        List<PracticeQuestion> hardPracticeQuestions = practiceQuestionRepository.findRandomByLevel(QuestionLevel.HARD, PageRequest.of(0,hard));

        PracticeSet saved = practiceSetService.createQuestionSet(practiceSet);

        List<PracticeQuestion> allPracticeQuestions = new ArrayList<>();
        allPracticeQuestions.addAll(easyPracticeQuestions);
        allPracticeQuestions.addAll(mediumPracticeQuestions);
        allPracticeQuestions.addAll(hardPracticeQuestions);

        List<PracticeSetItem> practiceSetItems = new ArrayList<>();

        for(int i = 0; i < allPracticeQuestions.size(); i++){
            PracticeSetItem practiceSetItem = new PracticeSetItem();
            practiceSetItem.setPracticeSet(saved);
            practiceSetItem.setPracticeQuestion(allPracticeQuestions.get(i));
            practiceSetItem.setOrderIndex(i+1);
            practiceSetItems.add(practiceSetItem);
        }
        return practiceSetItemRepository.saveAll(practiceSetItems);

    }
}
