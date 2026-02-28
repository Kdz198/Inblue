package fpt.org.inblue.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest.*;
import fpt.org.inblue.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping ("/api/interview-sessions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    @PostMapping("/generate-job-requirement")
    public JobRequirementData generateJobRequirement(@RequestBody String jobDescription) {
        return interviewSessionService.getJobRequirementFromJD(jobDescription);
    }

    @GetMapping("/config-options")
    public Map<String, Object> getInterviewConfigOptions() {
        return interviewSessionService.getInterviewConfigOptions();
    }

    @PostMapping("/create-session")
    public String createInterviewSession(@RequestBody InterviewSetupRequest request)  {
        return interviewSessionService.createSession(request);
    }

    @GetMapping("/user/{userId}")
    public List<InterviewSession> getAllSessionsForUser(@PathVariable Integer userId) {
        return interviewSessionService.getAllSessionsForUser(userId);
    }

    @GetMapping("cache/{sessionKey}")
    public InterviewSessionRedis getSessionFromCache(@PathVariable String sessionKey) {
        return interviewSessionService.getSessionFromCache(sessionKey);
    }

    @GetMapping("{sessionId}")
    public InterviewSession getSessionById(@PathVariable Integer sessionId) {
        return interviewSessionService.getSessionById(sessionId);
    }
}
