package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import fpt.org.inblue.service.QuestionCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionCategoryServiceImpl implements QuestionCategoryService {
    private final QuestionCategoryRepository questionCategoryRepository;

    @Override
    public QuestionCategory getQuestionCategory(int id) {
        return questionCategoryRepository.findById(id).get();
    }

    @Override
    public List<QuestionCategory> getAllQuestionCategories() {
        return questionCategoryRepository.findAll();
    }

    @Override
    public QuestionCategory saveQuestionCategory(QuestionCategory questionCategory) {
        return questionCategoryRepository.save(questionCategory);
    }

    @Override
    public QuestionCategory updateQuestionCategory(QuestionCategory questionCategory) {
        if (questionCategoryRepository.existsById(questionCategory.getId())) {
            return questionCategoryRepository.save(questionCategory);
        } else {
            throw new CustomException(
                    "Question category with id " + questionCategory.getId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionCategory(int id) {
        if (!questionCategoryRepository.existsById(id)) {
            throw new CustomException("Question category with id " + id + " does not exist.", HttpStatus.NOT_FOUND);
        }
        questionCategoryRepository.deleteById(id);
    }
}
