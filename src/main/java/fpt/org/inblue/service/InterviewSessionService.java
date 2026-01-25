package fpt.org.inblue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest.*;

import java.util.Map;

public interface InterviewSessionService {

    JobRequirementData getJobRequirementFromJD(String jobDescription);
    Map<String, Object> getInterviewConfigOptions();
    String createSession(InterviewSetupRequest request) ;
}
