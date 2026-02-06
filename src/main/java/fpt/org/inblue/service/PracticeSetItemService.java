package fpt.org.inblue.service;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.PracticeSetItem;

import java.util.List;

public interface PracticeSetItemService {
    PracticeSetItem createQuestionSetItem(PracticeSetItem practiceSetItem);
    PracticeSetItem getQuestionSetItem(int id);
    PracticeSetItem updateQuestionSetItem(PracticeSetItem practiceSetItem);
    void deleteQuestionSetItem(int id);
    List<PracticeSetItem> getAllQuestionSetItems();
    List<PracticeSetItem> getQuestionSetItemsByQuestionSet(int id);

    List<PracticeSetItem> createQuestionSetItems(int easy, int medium, int hard, PracticeSet practiceSet);
}
