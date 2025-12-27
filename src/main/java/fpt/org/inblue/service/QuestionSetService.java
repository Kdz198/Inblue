package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionSet;

import java.util.List;

public interface QuestionSetService {
    QuestionSet getQuestionSet(int id);
    QuestionSet createQuestionSet(QuestionSet questionSet);
    QuestionSet updateQuestionSet(QuestionSet questionSet);
    void deleteQuestionSet(int id);
    List<QuestionSet> getAllQuestionSets();
    List<QuestionSet> getQuestionSetsByTargetLevel(String id);
}
