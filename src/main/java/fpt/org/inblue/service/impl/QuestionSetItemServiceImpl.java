package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionSetItem;
import fpt.org.inblue.repository.QuestionSetItemRepository;
import fpt.org.inblue.service.QuestionSetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionSetItemServiceImpl implements QuestionSetItemService {
    @Autowired
    private QuestionSetItemRepository questionSetItemRepository;

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
}
