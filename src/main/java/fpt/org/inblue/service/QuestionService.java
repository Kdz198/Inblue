package fpt.org.inblue.service;

import fpt.org.inblue.model.Question;

import java.util.List;

public interface QuestionService {
    Question createQuestion(Question question);
    Question updateQuestion(Question question);
    void deleteQuestion(int id);
    Question getQuestionById(int id);
    List<Question> getAllQuestions();

    List<Question> getQuestionsByCategoryAndLevel(int categoryId, String level);

}
