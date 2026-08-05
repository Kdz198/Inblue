package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.CandidateProfileRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.InterviewSessionService;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.utils.HelperUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationDetailServiceImpl implements ApplicationDetailService {
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationService applicationService;
    private final RoundRepository roundRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final InterviewSessionService interviewSessionService;
    private final fpt.org.inblue.repository.InterviewSessionRepository interviewSessionRepository;
    private final MentorService mentorService;
    private final JwtUtils jwtUtils;

    @Override
    public ApplicationDetail getApplicationDetailById(long id) {
        return applicationDetailRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Application Detail not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<ApplicationDetail> getByApplicationId(long applicationDetailId) {
        return applicationDetailRepository.findAllByApplicationId(applicationDetailId);
    }

    @Override
    @Transactional
    public void hrScore(long applicationDetailId, boolean isPass, String note, double score) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        if (applicationDetail.getStatus() == ApplicationDetailStatus.COMPLETED) {
            throw new CustomException("Vòng thi này đã được HR duyệt kết quả trước đó", HttpStatus.BAD_REQUEST);
        }
        applicationDetail.setHrScore(score);
        applicationDetail.setHrNote(note);
        applicationDetail.setFinalScore(score);
        applicationDetail.setFinalResult(
                isPass ? ApplicationDetail.RoundResult.PASSED : ApplicationDetail.RoundResult.FAILED);
        applicationDetail.setCompletedAt(LocalDateTime.now());
        applicationDetail.setStatus(ApplicationDetailStatus.COMPLETED);
        applicationDetailRepository.save(applicationDetail);
        Application application = applicationService.getApplicationById(applicationDetail.getApplicationId());
        applicationService.moveToNextRound(application);
    }

    @Override
    public List<ApplicationDetail> getApplicationDetailsForReviewer() {
        String token = HelperUtil.getToke();
        int reviewerId = jwtUtils.getUserIdFromToken(token);
        List<String> roles = jwtUtils.getRolesFromToken(token);

        if (roles == null || roles.stream().noneMatch(role -> role.equals("ROLE_STAFF"))) {
            throw new CustomException("Only STAFF role is allowed to review application details", HttpStatus.FORBIDDEN);
        }

        return applicationDetailRepository.findAllByReviewerId(reviewerId);
    }

    @Override
    @Transactional
    public ApplicationDetail assignMentor(long applicationDetailId, int mentorId) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        if (applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_MENTOR
                && applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR) {
            throw new CustomException(
                    "Application detail status is not AWAITING_MENTOR or AWAITING_CANDIDATE_SELECT_MENTOR",
                    HttpStatus.BAD_REQUEST);
        }
        Round round = roundRepository
                .findById(applicationDetail.getRoundId())
                .orElseThrow(() -> new CustomException("Round not found", HttpStatus.NOT_FOUND));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime endTime = null;
        if (round.getConfigData() != null && round.getConfigData().getTimeLimitMinutes() != null) {
            endTime = now.plusMinutes(round.getConfigData().getTimeLimitMinutes());
        }

        ApplicationDetail.RoundSessionInfo sessionInfo = applicationDetail.getSessionInfo();
        if (sessionInfo == null) {
            sessionInfo = new ApplicationDetail.RoundSessionInfo();
        }
        sessionInfo.setStartTime(now);
        sessionInfo.setEndTime(endTime);

        applicationDetail.setSessionInfo(sessionInfo);
        applicationDetail.setMentorId(mentorId);
        applicationDetail.setStatus(ApplicationDetailStatus.PENDING);
        return applicationDetailRepository.save(applicationDetail);
    }

    @Override
    @Transactional
    public ApplicationDetail assignMentors(long applicationDetailId, List<Integer> mentorIds) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        if (applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_MENTOR
                && applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR) {
            throw new CustomException(
                    "Application detail status is not AWAITING_MENTOR or AWAITING_CANDIDATE_SELECT_MENTOR",
                    HttpStatus.BAD_REQUEST);
        }
        if (mentorIds == null || mentorIds.isEmpty()) {
            throw new CustomException("Mentor IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }

        applicationDetail.setAssignedMentorIds(mentorIds);
        applicationDetail.setMentorId(null);
        applicationDetail.setStatus(ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR);
        return applicationDetailRepository.save(applicationDetail);
    }

    @Override
    @Transactional
    public ApplicationDetail selectMentor(long applicationDetailId, int mentorId) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        if (applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR) {
            throw new CustomException(
                    "Application detail is not in AWAITING_CANDIDATE_SELECT_MENTOR status", HttpStatus.BAD_REQUEST);
        }
        if (applicationDetail.getAssignedMentorIds() == null
                || !applicationDetail.getAssignedMentorIds().contains(mentorId)) {
            throw new CustomException(
                    "Selected mentor is not in the assigned mentor list for this round", HttpStatus.BAD_REQUEST);
        }

        Round round = roundRepository
                .findById(applicationDetail.getRoundId())
                .orElseThrow(() -> new CustomException("Round not found", HttpStatus.NOT_FOUND));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime endTime = null;
        if (round.getConfigData() != null && round.getConfigData().getTimeLimitMinutes() != null) {
            endTime = now.plusMinutes(round.getConfigData().getTimeLimitMinutes());
        }

        ApplicationDetail.RoundSessionInfo sessionInfo = applicationDetail.getSessionInfo();
        if (sessionInfo == null) {
            sessionInfo = new ApplicationDetail.RoundSessionInfo();
        }
        sessionInfo.setStartTime(now);
        sessionInfo.setEndTime(endTime);

        applicationDetail.setSessionInfo(sessionInfo);
        applicationDetail.setMentorId(mentorId);
        applicationDetail.setStatus(ApplicationDetailStatus.PENDING);
        return applicationDetailRepository.save(applicationDetail);
    }

    @Override
    public List<MentorResponse> getAssignedMentors(long applicationDetailId) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        if (applicationDetail.getAssignedMentorIds() == null
                || applicationDetail.getAssignedMentorIds().isEmpty()) {
            return Collections.emptyList();
        }

        List<MentorResponse> mentorResponses = new ArrayList<>();
        for (Integer mentorId : applicationDetail.getAssignedMentorIds()) {
            try {
                MentorResponse mentorResponse = mentorService.getMentorById(mentorId);
                if (mentorResponse != null) {
                    mentorResponses.add(mentorResponse);
                }
            } catch (Exception e) {
                // Ignore if a mentor is deleted/not found
            }
        }
        return mentorResponses;
    }

    @Override
    @Transactional
    public String startAiInterview(long applicationDetailId) {
        ApplicationDetail appDetail = getApplicationDetailById(applicationDetailId);
        if (appDetail.getStatus() == ApplicationDetailStatus.COMPLETED) {
            throw new CustomException("This round is already completed", HttpStatus.BAD_REQUEST);
        }

        Application application = applicationService.getApplicationById(appDetail.getApplicationId());
        User applicant = userRepository
                .findById(application.getUserId())
                .orElseThrow(() -> new CustomException("Applicant not found", HttpStatus.NOT_FOUND));
        JobDescription jd = jobDescriptionRepository
                .findById(application.getJdId())
                .orElseThrow(() -> new CustomException("JobDescription not found", HttpStatus.NOT_FOUND));
        Round round = roundRepository
                .findById(appDetail.getRoundId())
                .orElseThrow(() -> new CustomException("Round not found", HttpStatus.NOT_FOUND));

        if (round.getRoundType() != fpt.org.inblue.enums.RoundType.AI_INTERVIEW) {
            throw new CustomException("This round is not AI_INTERVIEW", HttpStatus.BAD_REQUEST);
        }

        // Prepare Setup Request
        OrchestratorRequest.JobRequirementData requirementData =
                interviewSessionService.getJobRequirementFromJD(jd.getDescription());

        // Fetch Candidate Profile from DB
        CandidateProfile profile = candidateProfileRepository.findByApplicationId(appDetail.getApplicationId());
        if (profile == null) {
            throw new CustomException(
                    "Candidate profile not found. Please update your CV/Profile before starting the interview.",
                    HttpStatus.BAD_REQUEST);
        }

        OrchestratorRequest.SessionConfigData configData = new OrchestratorRequest.SessionConfigData();

        // Todo đoạn này chuẩn nếu có thơì gian thì phải lấy từ config của cái round đó
        if (configData.getInterviewMode() == null) {
            configData.setInterviewMode(fpt.org.inblue.enums.InterviewEnums.InterviewMode.STANDARD_MOCK);
        }
        if (configData.getLanguage() == null) {
            configData.setLanguage(fpt.org.inblue.enums.InterviewEnums.Language.VI);
        }
        if (configData.getDifficulty() == null) {
            configData.setDifficulty(fpt.org.inblue.enums.InterviewEnums.DifficultyLevel.FRESHER_BASIC);
        }
        if (configData.getDomain() == null) {
            configData.setDomain(fpt.org.inblue.enums.InterviewEnums.JobDomain.IT);
        }

        Integer duration = round.getConfigData() != null ? round.getConfigData().getTimeLimitMinutes() : 45;
        if (duration == null) duration = 45;
        configData.setDurationMinutes(duration);

        InterviewSetupRequest setupRequest = InterviewSetupRequest.builder()
                .userId(applicant.getId())
                .applicationDetailId(appDetail.getId())
                .candidateProfile(profile)
                .jobRequirement(requirementData)
                .sessionConfig(configData)
                .build();

        String sessionKey = interviewSessionService.createSession(setupRequest);

        InterviewSession interviewSession = interviewSessionRepository.findBySessionKey(sessionKey);
        if (interviewSession != null) {
            appDetail.setAiInterviewSessionId(interviewSession.getId());
            applicationDetailRepository.save(appDetail);
        }

        return sessionKey;
    }
}
