package fpt.org.inblue.service;

import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;

import java.util.*;

public interface QuizSetService {
    QuizSet createQuizSet(int practiceSetId, String QuizName);
    QuizSet getQuizById(int quizId);
    List<QuizSet> getHistoryByPracticeSet(int practiceSetId);
    QuizSet submitAndCalculateScore(int quizId, Map<Integer, String> userAnswers);
    void deleteQuizSet(int quizId);
    List<QuizItem> createFullQuizSet(int practiceSetId, String QuizName, List<QuizItemCreateRequest> items);
    List<QuizSet> getAllQuizSet();

}
