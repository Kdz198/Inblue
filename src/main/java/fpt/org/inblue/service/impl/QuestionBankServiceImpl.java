package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.QuestionBankMapper;
import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.model.dto.request.CreateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.UpdateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.QuestionGenerateRequest;
import fpt.org.inblue.model.dto.response.QuestionGenerateResponse;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class QuestionBankServiceImpl implements QuestionBankService {
    @Autowired
    private QuestionBankRepository questionBankRepository;
    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;
    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private ApiClient apiClient;

    @Override
    public QuestionBank createQuestionBank(CreateQuestionBankRequest request) {
        QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                .orElseThrow(() -> new CustomException(
                        "Question category not found with id: " + request.getQuestionCategoryId(),
                        HttpStatus.NOT_FOUND));
        QuestionBank questionBank = questionBankMapper.toEntity(request);
        questionBank.setQuestionCategory(category);
        return questionBankRepository.save(questionBank);
    }

    @Override
    public QuestionBank getQuestionBankById(Integer id) {
        return questionBankRepository.getQuestionBankById(id);
    }

    @Override
    public QuestionBank updateQuestionBank(Integer id, UpdateQuestionBankRequest request) {
        QuestionBank existing = questionBankRepository.findById(id)
                .orElseThrow(() -> new CustomException("Question bank not found with id: " + id, HttpStatus.NOT_FOUND));
        questionBankMapper.updateQuestionBankFromRequest(request, existing);
        if (request.getQuestionCategoryId() != null) {
            QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                    .orElseThrow(() -> new CustomException(
                            "Question category not found with id: " + request.getQuestionCategoryId(),
                            HttpStatus.NOT_FOUND));
            existing.setQuestionCategory(category);
        }
        return questionBankRepository.save(existing);
    }

    @Override
    public void deleteQuestionBank(Integer id) {
        if(!questionBankRepository.existsById(id)){
            throw new CustomException("Question bank not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        else{
            QuestionBank questionBank = questionBankRepository.getQuestionBankById(id);
            questionBank.setIsDeleted(true);
            questionBankRepository.save(questionBank);
        }
    }

    @Override
    public List<QuestionBank> getAllQuestionBanks() {
        return questionBankRepository.findAll();
    }

    @Override
    public QuestionGenerateResponse generateQuestion(QuestionGenerateRequest request) {

        return apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.QUIZ_GEN,
                request,
                "java",
                true,
                null,
                QuestionGenerateResponse.class
        );
    }
}
