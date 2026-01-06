package fpt.org.inblue.service;

import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.model.QuestionSetItem;

import java.util.List;

public interface QuestionSetItemService {
    QuestionSetItem createQuestionSetItem(QuestionSetItem questionSetItem);
    QuestionSetItem getQuestionSetItem(int id);
    QuestionSetItem updateQuestionSetItem(QuestionSetItem questionSetItem);
    void deleteQuestionSetItem(int id);
    List<QuestionSetItem> getAllQuestionSetItems();
    List<QuestionSetItem> getQuestionSetItemsByQuestionSet(int id);

    List<QuestionSetItem> createQuestionSetItems(int easy, int medium, int hard, QuestionSet questionSet);
}
