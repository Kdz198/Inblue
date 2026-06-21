package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.dto.request.QuestionGenerateRequest;
import fpt.org.inblue.model.dto.response.QuestionGenerateResponse;

import java.util.List;

public interface QuestionBankService {
    QuestionBank createQuestionBank(QuestionBank questionBank);
    QuestionBank getQuestionBankById(Integer id);
    QuestionBank updateQuestionBank(Integer id, QuestionBank questionBank);
    void deleteQuestionBank(Integer id);
    List<QuestionBank> getAllQuestionBanks();
    QuestionGenerateResponse generateQuestion(QuestionGenerateRequest request);
}
