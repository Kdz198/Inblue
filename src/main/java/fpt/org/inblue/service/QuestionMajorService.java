package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionMajor;

import java.util.List;

public interface QuestionMajorService {
    QuestionMajor getQuestionMajorById(int id);
    QuestionMajor createQuestionMajor(QuestionMajor questionMajor);
    QuestionMajor updateQuestionMajor(QuestionMajor questionMajor);
    List<QuestionMajor> getAllQuestionMajors();
    boolean deleteQuestionMajor(int id);
}
