package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class QuestionBankServiceImpl implements QuestionBankService {
    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Override
    public QuestionBank createQuestionBank(QuestionBank questionBank) {
        return questionBankRepository.save(questionBank);
    }

    @Override
    public QuestionBank getQuestionBankById(Integer id) {
        return questionBankRepository.getQuestionBankById(id);
    }

    @Override
    public QuestionBank updateQuestionBank(Integer id, QuestionBank questionBank) {
       if(!questionBankRepository.existsById(id)){
           throw new CustomException("Question bank not found with id: " + id, HttpStatus.NOT_FOUND);
       }
        return questionBankRepository.save(questionBank);
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
}
