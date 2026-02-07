package fpt.org.inblue.service;

import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;

import java.util.List;

public interface QuizItemService {
    List<QuizItem> saveAllItems(List<QuizItemCreateRequest> items, int quizSetId);
    List<QuizItem> getItemsByQuizSetId(int quizId);
}
