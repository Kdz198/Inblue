package fpt.org.inblue.service.impl;


import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateAIRequest;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.repository.PracticeSetRepository;
import fpt.org.inblue.repository.QuizItemRepository;
import fpt.org.inblue.repository.QuizSetRepository;
import fpt.org.inblue.service.PythonApiClient;
import fpt.org.inblue.service.QuizItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizItemServiceImpl implements QuizItemService {
    @Autowired
    private QuizItemRepository quizItemRepository;
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public List<QuizItem> saveAllItems(List<QuizItemCreateRequest> dtos) {

        List<QuizItem> items = dtos.stream().map(dto -> {
            try {
                return QuizItem.builder()
                        .question(dto.getQuestion())
                        .options(objectMapper.writeValueAsString(dto.getOptions()))
                        // Nén Map thành JSON
                        .correctAnswer(dto.getCorrectAnswer())
                        .explanation(dto.getExplanation())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return quizItemRepository.saveAll(items);
    }



}
