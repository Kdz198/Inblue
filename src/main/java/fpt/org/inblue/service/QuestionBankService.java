package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.dto.request.CreateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.QuestionGenerateRequest;
import fpt.org.inblue.model.dto.request.UpdateQuestionBankRequest;
import fpt.org.inblue.model.dto.response.QuestionGenerateResponse;
import java.util.List;

public interface QuestionBankService {
    QuestionBank createQuestionBank(CreateQuestionBankRequest request);

    QuestionBank getQuestionBankById(Integer id);

    QuestionBank updateQuestionBank(Integer id, UpdateQuestionBankRequest request);

    void deleteQuestionBank(Integer id);

    List<QuestionBank> getAllQuestionBanks();

    QuestionGenerateResponse generateQuestion(QuestionGenerateRequest request);
}
