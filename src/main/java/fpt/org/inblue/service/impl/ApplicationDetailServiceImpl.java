package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest;
import fpt.org.inblue.model.dto.response.MentorPendingScheduleResponse;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.model.dto.response.ReviewerApplicationDetailResponseDto;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.InterviewSessionService;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.service.SessionService;
import fpt.org.inblue.utils.HelperUtil;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationDetailServiceImpl implements ApplicationDetailService {
    private static final int MAX_REJECT_REASON_LENGTH = 1000;

    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final RoundRepository roundRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final InterviewSessionService interviewSessionService;
    private final InterviewSessionRepository interviewSessionRepository;
    private final MentorService mentorService;
    private final MentorRepository mentorRepository;
    private final SessionService sessionService;
    private final JwtUtils jwtUtils;

    @Override
    public ApplicationDetail getApplicationDetailById(long id) {
        return applicationDetailRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Application Detail not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<ApplicationDetail> getByApplicationId(long applicationId) {
        return applicationDetailRepository.findAllByApplicationId(applicationId);
    }

    @Override
    @Transactional
    public void hrScore(long applicationDetailId, boolean isPass, String note, double score) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
        applicationDetail.setHrScore(score);
        applicationDetail.setHrNote(note);

        if (isPass) {
            applicationDetail.setFinalResult(ApplicationDetail.RoundResult.PASSED);
        } else {
            applicationDetail.setFinalResult(ApplicationDetail.RoundResult.FAILED);
        }
        applicationDetail.setFinalScore(score);

        applicationDetail.setStatus(ApplicationDetailStatus.COMPLETED);
        applicationDetail.setCompletedAt(java.time.LocalDateTime.now());
        applicationDetailRepository.save(applicationDetail);

        Application application = applicationService.getApplicationById(applicationDetail.getApplicationId());
        applicationService.moveToNextRound(application);
    }

    @Override
    public List<ReviewerApplicationDetailResponseDto> getApplicationDetailsForReviewer() {
        String token = HelperUtil.getToke();
        int reviewerId = jwtUtils.getUserIdFromToken(token);
        List<String> roles = jwtUtils.getRolesFromToken(token);

        if (roles == null || roles.stream().noneMatch(role -> role.equals("ROLE_STAFF"))) {
            throw new CustomException("Only STAFF role is allowed to review application details", HttpStatus.FORBIDDEN);
        }

        List<ApplicationDetail> details = applicationDetailRepository.findAllByReviewerId(reviewerId);
        List<ReviewerApplicationDetailResponseDto> result = new ArrayList<>();

        for (ApplicationDetail detail : details) {
            String jobTitle = "N/A";
            String roundName = "Unknown Round";
            String instruction = null;
            Round.RoundConfig roundConfig = null;

            if (detail.getApplicationId() != null) {
                try {
                    Application app = applicationService.getApplicationById(detail.getApplicationId());
                    if (app != null && app.getJdId() != null) {
                        Optional<JobDescription> jdOpt = jobDescriptionRepository.findById(app.getJdId());
                        if (jdOpt.isPresent()) {
                            jobTitle = jdOpt.get().getTitle();
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            if (detail.getRoundId() != null) {
                Optional<Round> roundOpt = roundRepository.findById(detail.getRoundId());
                if (roundOpt.isPresent()) {
                    Round round = roundOpt.get();
                    roundName = round.getName() != null ? round.getName() : "Unknown Round";
                    roundConfig = round.getConfigData();
                    if (roundConfig != null) {
                        instruction = roundConfig.getInstruction() != null
                                ? roundConfig.getInstruction()
                                : roundConfig.getEvaluationCriteria();
                    }
                }
            }

            ReviewerApplicationDetailResponseDto dto = ReviewerApplicationDetailResponseDto.builder()
                    .id(detail.getId())
                    .applicationId(detail.getApplicationId())
                    .roundId(detail.getRoundId())
                    .status(detail.getStatus())
                    .finalScore(detail.getFinalScore())
                    .submissionData(detail.getSubmissionData())
                    .aiScore(detail.getAiScore())
                    .aiFeedback(detail.getAiFeedback())
                    .structuredAiFeedback(detail.getStructuredAiFeedback())
                    .hrScore(detail.getHrScore())
                    .hrNote(detail.getHrNote())
                    .finalResult(detail.getFinalResult())
                    .startedAt(detail.getStartedAt())
                    .completedAt(detail.getCompletedAt())
                    .mentorId(detail.getMentorId())
                    .assignedMentorIds(detail.getAssignedMentorIds())
                    .mentorReview(detail.getMentorReview())
                    .sessionId(detail.getSessionId())
                    .aiInterviewSessionId(detail.getAiInterviewSessionId())
                    .sessionInfo(detail.getSessionInfo())
                    .createdAt(detail.getCreatedAt())
                    .updatedAt(detail.getUpdatedAt())
                    .jobTitle(jobTitle)
                    .roundName(roundName)
                    .instruction(instruction)
                    .roundConfig(roundConfig)
                    .build();

            result.add(dto);
        }

        return result;
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

    /**
     * Xác định Mentor.id của người đang đăng nhập.
     * Khi mentor login, JWT subject chính là Mentor.id (xem CustomUserDetailService), nhưng nếu
     * tài khoản đó đồng thời tồn tại ở bảng User thì subject sẽ là User.id -> fallback tra theo email.
     */
    private int resolveCurrentMentorId() {
        String token = HelperUtil.getToke();
        if (token == null || token.isBlank()) {
            throw new CustomException("Missing Authorization token", HttpStatus.UNAUTHORIZED);
        }
        Integer tokenId = jwtUtils.getUserIdFromToken(token);
        String email = jwtUtils.getEmailFromToken(token);

        if (tokenId != null) {
            Mentor byId = mentorRepository.getMentorById(tokenId);
            if (byId != null && (email == null || email.equalsIgnoreCase(byId.getEmail()))) {
                return byId.getId();
            }
        }
        if (email != null && !email.isBlank()) {
            Mentor byEmail = mentorRepository.findByEmail(email);
            if (byEmail != null) {
                return byEmail.getId();
            }
        }
        throw new CustomException("Chỉ mentor mới được thực hiện thao tác này", HttpStatus.FORBIDDEN);
    }

    @Override
    @Transactional
    public ApplicationDetail scheduleDecision(long applicationDetailId, boolean approved, String reason) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);

        if (applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_MENTOR_SCHEDULE_APPROVAL) {
            throw new CustomException(
                    "Vòng phỏng vấn này không có lịch hẹn nào đang chờ duyệt", HttpStatus.BAD_REQUEST);
        }
        if (applicationDetail.getMentorId() == null) {
            throw new CustomException("Mentor has not been assigned to this round", HttpStatus.BAD_REQUEST);
        }

        int currentMentorId = resolveCurrentMentorId();
        if (currentMentorId != applicationDetail.getMentorId()) {
            throw new CustomException("Bạn không phải mentor được gán cho vòng phỏng vấn này", HttpStatus.FORBIDDEN);
        }

        ApplicationDetail.RoundSessionInfo sessionInfo = applicationDetail.getSessionInfo();
        if (sessionInfo == null) {
            sessionInfo = new ApplicationDetail.RoundSessionInfo();
        }

        if (approved) {
            String pendingJoinTimeRaw = sessionInfo.getPendingJoinTime();
            if (pendingJoinTimeRaw == null || pendingJoinTimeRaw.isBlank()) {
                throw new CustomException("Không tìm thấy lịch hẹn đang chờ duyệt", HttpStatus.BAD_REQUEST);
            }
            LocalDateTime pendingJoinTime;
            try {
                pendingJoinTime = LocalDateTime.parse(pendingJoinTimeRaw);
            } catch (Exception e) {
                throw new CustomException("Lịch hẹn đang chờ duyệt không hợp lệ", HttpStatus.BAD_REQUEST);
            }
            int duration =
                    sessionInfo.getPendingDurationMinutes() != null ? sessionInfo.getPendingDurationMinutes() : 60;

            Application application = applicationService.getApplicationById(applicationDetail.getApplicationId());

            Session session = sessionService.createRoomForApprovedSchedule(
                    application.getUserId(),
                    applicationDetail.getMentorId(),
                    Timestamp.valueOf(pendingJoinTime),
                    duration);

            sessionInfo.setSessionId(session.getId());
            sessionInfo.setMeetingType(fpt.org.inblue.enums.MeetingType.ONLINE);
            sessionInfo.setPendingJoinTime(null);
            sessionInfo.setPendingDurationMinutes(null);
            sessionInfo.setMentorRejectReason(null);
            sessionInfo.setMentorRejectedAt(null);
            sessionInfo.setRejectedMentorId(null);

            applicationDetail.setSessionId(session.getId());
            applicationDetail.setSessionInfo(sessionInfo);
            // Quay lại PENDING (đã có sessionId) -> ứng viên vào trạng thái chờ tới giờ phỏng vấn
            applicationDetail.setStatus(ApplicationDetailStatus.PENDING);
            return applicationDetailRepository.save(applicationDetail);
        }

        // ===== Mentor từ chối lịch hẹn =====
        if (reason == null || reason.trim().isEmpty()) {
            throw new CustomException("Vui lòng nhập lý do từ chối lịch hẹn", HttpStatus.BAD_REQUEST);
        }
        String trimmedReason = reason.trim();
        if (trimmedReason.length() > MAX_REJECT_REASON_LENGTH) {
            throw new CustomException(
                    "Lý do từ chối không được vượt quá " + MAX_REJECT_REASON_LENGTH + " ký tự", HttpStatus.BAD_REQUEST);
        }

        int rejectedMentorId = applicationDetail.getMentorId();

        sessionInfo.setPendingJoinTime(null);
        sessionInfo.setPendingDurationMinutes(null);
        // Xoá luôn hình thức họp đã chọn: ứng viên sẽ chọn lại từ đầu.
        // (FE dùng điều kiện meetingType == null để cho phép đặt lại lịch)
        sessionInfo.setMeetingType(null);
        sessionInfo.setMentorRejectReason(trimmedReason);
        sessionInfo.setMentorRejectedAt(LocalDateTime.now().toString());
        sessionInfo.setRejectedMentorId(rejectedMentorId);

        applicationDetail.setMentorId(null);
        applicationDetail.setSessionInfo(sessionInfo);

        if (applicationDetail.getAssignedMentorIds() != null
                && !applicationDetail.getAssignedMentorIds().isEmpty()) {
            // Giữ nguyên danh sách mentor Admin đã đề xuất (kể cả mentor vừa từ chối)
            // để ứng viên chọn lại, không loại rejectedMentorId ra khỏi danh sách nữa.
            applicationDetail.setStatus(ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR);
        } else {
            // Vòng này được Admin gán thẳng 1 mentor (assign-mentor), không có danh sách để chọn lại
            applicationDetail.setStatus(ApplicationDetailStatus.AWAITING_MENTOR);
        }

        return applicationDetailRepository.save(applicationDetail);
    }

    @Override
    public List<MentorPendingScheduleResponse> getPendingScheduleApprovals() {
        int mentorId = resolveCurrentMentorId();
        List<ApplicationDetail> pendingDetails = applicationDetailRepository.findAllByMentorIdAndStatus(
                mentorId, ApplicationDetailStatus.AWAITING_MENTOR_SCHEDULE_APPROVAL);

        List<Long> applicationIds = pendingDetails.stream()
                .map(ApplicationDetail::getApplicationId)
                .distinct()
                .toList();
        Map<Long, Application> applicationsById = applicationRepository.findAllById(applicationIds).stream()
                .collect(Collectors.toMap(Application::getId, a -> a));

        List<Integer> candidateUserIds = applicationsById.values().stream()
                .map(Application::getUserId)
                .distinct()
                .toList();
        Map<Integer, User> candidatesByUserId =
                userRepository.findAllById(candidateUserIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Long> jdIds = applicationsById.values().stream()
                .map(Application::getJdId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> jobTitlesByJdId = jobDescriptionRepository.findAllById(jdIds).stream()
                .collect(Collectors.toMap(JobDescription::getId, JobDescription::getTitle));

        List<Long> roundIds = pendingDetails.stream()
                .map(ApplicationDetail::getRoundId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Round> roundsById =
                roundRepository.findAllById(roundIds).stream().collect(Collectors.toMap(Round::getId, r -> r));

        List<MentorPendingScheduleResponse> responses = new ArrayList<>();
        for (ApplicationDetail detail : pendingDetails) {
            ApplicationDetail.RoundSessionInfo sessionInfo = detail.getSessionInfo();
            Application application = applicationsById.get(detail.getApplicationId());
            User candidate = application != null ? candidatesByUserId.get(application.getUserId()) : null;
            String jobTitle = application != null ? jobTitlesByJdId.get(application.getJdId()) : null;
            Round round = detail.getRoundId() != null ? roundsById.get(detail.getRoundId()) : null;

            responses.add(MentorPendingScheduleResponse.builder()
                    .applicationDetailId(detail.getId())
                    .applicationId(detail.getApplicationId())
                    .roundId(detail.getRoundId())
                    .roundName(round != null ? round.getName() : null)
                    .roundOrder(round != null ? round.getRoundOrder() : null)
                    .jobTitle(jobTitle)
                    .candidateUserId(application != null ? application.getUserId() : null)
                    .candidateName(candidate != null ? candidate.getName() : null)
                    .candidateEmail(candidate != null ? candidate.getEmail() : null)
                    .candidateAvatarUrl(candidate != null ? candidate.getAvatarUrl() : null)
                    .mentorId(detail.getMentorId())
                    .meetingType(sessionInfo != null ? sessionInfo.getMeetingType() : null)
                    .proposedJoinTime(sessionInfo != null ? sessionInfo.getPendingJoinTime() : null)
                    .proposedDurationMinutes(sessionInfo != null ? sessionInfo.getPendingDurationMinutes() : null)
                    .requestedAt(detail.getUpdatedAt())
                    .build());
        }
        return responses;
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
        configData.setDurationMinutes(duration);
        configData.setEvaluationCriteria(
                round.getConfigData() != null ? round.getConfigData().getEvaluationCriteria() : null);
        configData.setAdditionalInstructions(
                round.getConfigData() != null ? round.getConfigData().getAiSystemPrompt() : null);

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
