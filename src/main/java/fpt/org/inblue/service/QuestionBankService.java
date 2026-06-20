package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionBank;

import java.util.List;

public interface QuestionBankService {
    QuestionBank createQuestionBank(QuestionBank questionBank);
    QuestionBank getQuestionBankById(Integer id);
    QuestionBank updateQuestionBank(Integer id, QuestionBank questionBank);
    void deleteQuestionBank(Integer id);
    List<QuestionBank> getAllQuestionBanks();
}
