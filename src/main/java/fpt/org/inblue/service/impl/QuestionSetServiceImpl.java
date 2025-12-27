package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.model.enums.TargetLevel;
import fpt.org.inblue.repository.QuestionSetRepository;
import fpt.org.inblue.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionSetServiceImpl implements QuestionSetService {
    @Autowired
    private QuestionSetRepository questionSetRepository;

    @Override
    public QuestionSet getQuestionSet(int id) {
        return questionSetRepository.findById(id).orElse(null);
    }

    @Override
    public QuestionSet createQuestionSet(QuestionSet questionSet) {
        return questionSetRepository.save(questionSet);
    }

    @Override
    public QuestionSet updateQuestionSet(QuestionSet questionSet) {
        if(questionSetRepository.existsById(questionSet.getQuestionSetId())){
            return questionSetRepository.save(questionSet);
        }
        else{
            throw new CustomException("question set not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteQuestionSet(int id) {
        if(questionSetRepository.existsById(id)){
            questionSetRepository.deleteById(id);
        }
        else{
            throw new CustomException("question set not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<QuestionSet> getAllQuestionSets() {
        return questionSetRepository.findAll();
    }

    @Override
    public List<QuestionSet> getQuestionSetsByTargetLevel(String level) {
        return questionSetRepository.findAllByLevel(TargetLevel.valueOf(level));
    }
}
