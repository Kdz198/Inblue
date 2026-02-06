package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionLesson;
import fpt.org.inblue.repository.QuestionLessonRepository;
import fpt.org.inblue.service.QuestionLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionLessonServiceImpl implements QuestionLessonService {
    @Autowired
    private QuestionLessonRepository questionLessonRepository;
    @Override
    public QuestionLesson getQuestionCategory(int id) {
        return questionLessonRepository.findById(id).get();
    }

    @Override
    public QuestionLesson createQuestionLesson(QuestionLesson questionLesson) {
        return questionLessonRepository.save(questionLesson);
    }

    @Override
    public QuestionLesson updateQuestionCategory(QuestionLesson questionLesson) {
        if(questionLessonRepository.existsById(questionLesson.getId())){
            return questionLessonRepository.save(questionLesson);
        }
        else {
            throw new CustomException("Question Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionCategory(int id) {
        if(questionLessonRepository.existsById(id)){
            questionLessonRepository.deleteById(id);
        }
        else {
            throw new CustomException("Question Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<QuestionLesson> getAllQuestionCategories() {
        return questionLessonRepository.findAll();
    }

    @Override
    public QuestionLesson findByName(String lessonName) {
       return questionLessonRepository.findByLessonName(lessonName);
    }


}
