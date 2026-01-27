package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.QuestionResponse;

public interface InterviewProcessService {
     QuestionResponse getCurrentQuestion(String sessionKey);
    QuestionResponse submitAnswer(SubmitAnswerRequest request);
}
