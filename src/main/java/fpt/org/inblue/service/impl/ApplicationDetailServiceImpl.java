package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.utils.HelperUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.model.Round;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationDetailServiceImpl implements ApplicationDetailService {
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationService applicationService;
    private final RoundRepository roundRepository;
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
    public void hrScore(long applicationDetailId, boolean isPass, String note, double score) {
        ApplicationDetail applicationDetail = getApplicationDetailById(applicationDetailId);
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
        if (applicationDetail.getStatus() != ApplicationDetailStatus.AWAITING_MENTOR) {
            throw new CustomException("Application detail status is not AWAITING_MENTOR", HttpStatus.BAD_REQUEST);
        }
        Round round = roundRepository.findById(applicationDetail.getRoundId())
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
}
