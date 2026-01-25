package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionLesson;

import java.util.List;

public interface QuestionLessonService {
    QuestionLesson getQuestionCategory(int id);
    QuestionLesson createQuestionCategory(QuestionLesson questionLesson);
    QuestionLesson updateQuestionCategory(QuestionLesson questionLesson);
    void deleteQuestionCategory(int id);
    List<QuestionLesson> getAllQuestionCategories();

}
