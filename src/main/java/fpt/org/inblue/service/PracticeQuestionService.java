package fpt.org.inblue.service;

import fpt.org.inblue.model.PracticeQuestion;

import java.util.List;

public interface PracticeQuestionService {
    PracticeQuestion createQuestion(PracticeQuestion practiceQuestion);
    PracticeQuestion updateQuestion(PracticeQuestion practiceQuestion);
    void deleteQuestion(int id);
    PracticeQuestion getQuestionById(int id);
    List<PracticeQuestion> getAllQuestions();

    List<PracticeQuestion> getQuestionsByCategoryAndLevel(int categoryId, String level);

    List<PracticeQuestion> getRandomQuestionsByLevel(String level, int count);

    List<PracticeQuestion> createQuestionList(List<PracticeQuestion> practiceQuestions);

}
