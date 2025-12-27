package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Question;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import fpt.org.inblue.repository.QuestionRepository;
import fpt.org.inblue.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Override
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Question updateQuestion(Question question) {
        if (questionRepository.existsById(question.getQuestionId())){
            return questionRepository.save(question);
        } else {
            throw new CustomException("Question not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestion(int id) {
        if (questionRepository.existsById(id)){
            questionRepository.deleteById(id);
        } else {
            throw new CustomException("Question not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Question getQuestionById(int id) {
        return questionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> getQuestionsByCategoryAndLevel(int categoryId, String level) {
        return questionRepository.findAllByCategory_IdAndLevel(categoryId, QuestionLevel.valueOf(level));
    }
}
