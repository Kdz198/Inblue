package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionCategory;

import java.util.List;

public interface QuestionCategoryService {
    QuestionCategory getQuestionCategory(int id);
    List<QuestionCategory> getAllQuestionCategories();
    QuestionCategory saveQuestionCategory(QuestionCategory questionCategory);
    QuestionCategory updateQuestionCategory(QuestionCategory questionCategory);
    void deleteQuestionCategory(int id);
}
