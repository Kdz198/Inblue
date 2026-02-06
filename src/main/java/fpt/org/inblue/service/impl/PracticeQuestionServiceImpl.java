package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.enums.QuestionLevel;

import fpt.org.inblue.repository.PracticeQuestionRepository;
import fpt.org.inblue.service.PracticeQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PracticeQuestionServiceImpl implements PracticeQuestionService {
    @Autowired
    private PracticeQuestionRepository practiceQuestionRepository;
    @Override
    public PracticeQuestion createQuestion(PracticeQuestion practiceQuestion) {
        return practiceQuestionRepository.save(practiceQuestion);
    }

    @Override
    public PracticeQuestion updateQuestion(PracticeQuestion practiceQuestion) {
        if (practiceQuestionRepository.existsById(practiceQuestion.getQuestionId())){
            return practiceQuestionRepository.save(practiceQuestion);
        } else {
            throw new CustomException("Question not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestion(int id) {
        if (practiceQuestionRepository.existsById(id)){
            practiceQuestionRepository.deleteById(id);
        } else {
            throw new CustomException("Question not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public PracticeQuestion getQuestionById(int id) {
        return practiceQuestionRepository.findById(id).orElse(null);
    }

    @Override
    public List<PracticeQuestion> getAllQuestions() {
        return practiceQuestionRepository.findAll();
    }

    @Override
    public List<PracticeQuestion> getQuestionsByCategoryAndLevel(int categoryId, String level) {
        return practiceQuestionRepository.findAllByLesson_IdAndLevel(categoryId, QuestionLevel.valueOf(level));
    }

    @Override
    public List<PracticeQuestion> getRandomQuestionsByLevel(String level, int count) {
        return practiceQuestionRepository.findRandomByLevel(QuestionLevel.valueOf(level), PageRequest.of(0, count));
    }

    @Override
    public List<PracticeQuestion> createQuestionList(List<PracticeQuestion> practiceQuestions) {
        return practiceQuestionRepository.saveAll(practiceQuestions);
    }
}
