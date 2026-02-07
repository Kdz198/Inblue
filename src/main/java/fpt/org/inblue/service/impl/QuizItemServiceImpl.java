package fpt.org.inblue.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.repository.QuizItemRepository;
import fpt.org.inblue.repository.QuizSetRepository;
import fpt.org.inblue.service.QuizItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizItemServiceImpl implements QuizItemService {
    @Autowired
    private QuizItemRepository quizItemRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private QuizSetRepository quizSetRepository;

    @Override
    public List<QuizItem> saveAllItems(List<QuizItemCreateRequest> dtos, int quizSetId) {
        QuizSet quizSet = quizSetRepository.findById(quizSetId).get();

        List<QuizItem> items = dtos.stream().map(dto -> {
            try {
                return QuizItem.builder()
                        .question(dto.getQuestion())
                        .options(objectMapper.writeValueAsString(dto.getOptions()))
                        // Nén Map thành JSON
                        .correctAnswer(dto.getCorrectAnswer())
                        .explanation(dto.getExplanation())
                        .quizSet(quizSet)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return quizItemRepository.saveAll(items);
    }

    @Override
    public List<QuizItem> getItemsByQuizSetId(int quizId) {
        return quizItemRepository.findAllByQuizSet_QuizId(quizId);
    }
}
