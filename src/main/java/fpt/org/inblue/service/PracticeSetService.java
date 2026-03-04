package fpt.org.inblue.service;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.dto.request.PracticeAIRequest;
import fpt.org.inblue.model.dto.request.PracticeGenerateRequest;
import fpt.org.inblue.model.dto.request.PracticeRequest;
import fpt.org.inblue.model.dto.response.PracticeSetAIResponse;
import fpt.org.inblue.model.dto.response.PracticeSetResponse;

import java.util.List;

public interface PracticeSetService {
    PracticeSet getQuestionSet(int id);
    PracticeSet createQuestionSet(PracticeSet practiceSet);
    PracticeSet updateQuestionSet(PracticeSet practiceSet);
    void deleteQuestionSet(int id);
    List<PracticeSet> getAllQuestionSets();
    List<PracticeSet> getQuestionSetsByTargetLevel(String id);

    PracticeSet createFullSet(PracticeRequest practiceRequest);
    PracticeSetResponse getFullSet(int id);
    List<PracticeSetAIResponse> creatPracticeSetByAI(PracticeGenerateRequest request);
    void createFullSetByAI(PracticeRequest practiceRequest,int aiInterviewId);
    List<PracticeSetResponse> getAllByInterviewSession(int interviewSessionId);
}
