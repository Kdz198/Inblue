package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import fpt.org.inblue.service.QuestionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionCategoryServiceImpl implements QuestionCategoryService {
    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;
    @Override
    public QuestionCategory getQuestionCategory(int id) {
        return questionCategoryRepository.findById(id).get();
    }

    @Override
    public QuestionCategory createQuestionCategory(QuestionCategory questionCategory) {
        return questionCategoryRepository.save(questionCategory);
    }

    @Override
    public QuestionCategory updateQuestionCategory(QuestionCategory questionCategory) {
        if(questionCategoryRepository.existsById(questionCategory.getId())){
            return questionCategoryRepository.save(questionCategory);
        }
        else {
            throw new CustomException("Question Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionCategory(int id) {
        if(questionCategoryRepository.existsById(id)){
            questionCategoryRepository.deleteById(id);
        }
        else {
            throw new CustomException("Question Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<QuestionCategory> getAllQuestionCategories() {
        return questionCategoryRepository.findAll();
    }


}
