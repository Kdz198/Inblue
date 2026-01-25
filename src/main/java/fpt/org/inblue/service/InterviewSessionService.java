package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.OrchestratorRequest.*;

import java.util.Map;

public interface InterviewSessionService {

    JobRequirementData getJobRequirementFromJD(String jobDescription);
    Map<String, Object> getInterviewConfigOptions();
}
