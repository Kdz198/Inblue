package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionMajor;
import fpt.org.inblue.repository.QuestionMajorRepository;
import fpt.org.inblue.service.QuestionMajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionMajorServiceImpl implements QuestionMajorService {
    @Autowired
    private QuestionMajorRepository questionMajorRepository;

    @Override
    public QuestionMajor getQuestionMajorById(int id) {
        if(questionMajorRepository.existsById(id)){
            return questionMajorRepository.findById(id).get();
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public QuestionMajor createQuestionMajor(QuestionMajor questionMajor) {
        return questionMajorRepository.save(questionMajor);
    }

    @Override
    public QuestionMajor updateQuestionMajor(QuestionMajor questionMajor) {
        if(questionMajorRepository.existsById(questionMajor.getId())){
            return questionMajorRepository.save(questionMajor);
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<QuestionMajor> getAllQuestionMajors() {
        return questionMajorRepository.findAll();
    }

    @Override
    public boolean deleteQuestionMajor(int id) {
        if(questionMajorRepository.existsById(id)){
            questionMajorRepository.deleteById(id);
            return true;
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }
}
