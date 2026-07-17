package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.utils.HelperUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final JwtUtils jwtUtils;
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApplicationDetailRepository applicationDetailRepository;

    @Override
    public Application applyForJob(Long jdId) {
        String token = HelperUtil.getToke();
        int userId = jwtUtils.getUserIdFromToken(token);
        Application application = new Application();
        application.setUserId(userId);
        application.setJdId(jdId);
        JobDescription jd = jobDescriptionRepository
                .findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));
        jd.setAppliedCount(jd.getAppliedCount() + 1);
        jobDescriptionRepository.save(jd);
        Application savedApplication = applicationRepository.save(application);

        // Nếu vòng đầu tiên là MENTROR_REVIEW hoặc AI_INTERVIEW (không có luồng nộp bài),
        // cần tạo ApplicationDetail ngay lúc apply vì moveToNextRound() chưa bao giờ được gọi trước đó.
        if (jd.getRounds() != null && !jd.getRounds().isEmpty()) {
            Round firstRound = jd.getRounds().get(0);
            if (firstRound.getRoundType() == RoundType.MENTROR_REVIEW
                    || firstRound.getRoundType() == RoundType.AI_INTERVIEW) {
                ApplicationDetailStatus initialStatus = firstRound.getRoundType() == RoundType.MENTROR_REVIEW
                        ? ApplicationDetailStatus.AWAITING_MENTOR
                        : ApplicationDetailStatus.PENDING;
                java.time.LocalDateTime endTime = null;
                if (firstRound.getConfigData() != null
                        && firstRound.getConfigData().getTimeLimitMinutes() != null) {
                    endTime = java.time.LocalDateTime.now()
                            .plusMinutes(firstRound.getConfigData().getTimeLimitMinutes());
                }
                ApplicationDetail firstDetail = ApplicationDetail.builder()
                        .applicationId(savedApplication.getId())
                        .roundId(firstRound.getId())
                        .status(initialStatus)
                        .sessionInfo(ApplicationDetail.RoundSessionInfo.builder()
                                .startTime(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()))
                                .endTime(endTime != null ? java.sql.Timestamp.valueOf(endTime) : null)
                                .build())
                        .build();
                applicationDetailRepository.save(firstDetail);
            }
        }

        return savedApplication;
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public List<Application> getAllApplicationsByUserId() {
        String token = HelperUtil.getToke();
        int userId = jwtUtils.getUserIdFromToken(token);
        return applicationRepository.findAllByUserId(userId);
    }

    @Override
    public void moveToNextRound(Application currentApplication) {
        JobDescription jd =
                jobDescriptionRepository.findById(currentApplication.getJdId()).orElse(null);
        System.out.println("Moving application " + currentApplication.getId() + " to next round. Current round order: "
                + currentApplication.getCurrentRoundOrder());
        if (jd != null) {
            List<Round> rounds = jd.getRounds();
            int currentRoundOrder = currentApplication.getCurrentRoundOrder();
            if (currentRoundOrder < rounds.size()) {
                currentApplication.setCurrentRoundOrder(currentRoundOrder + 1);
                applicationRepository.save(currentApplication);
                System.out.println("Application " + currentApplication.getId() + " moved to round order "
                        + currentApplication.getCurrentRoundOrder());

                // Tự động tạo ApplicationDetail với status AWAITING_MENTOR cho vòng MENTROR_REVIEW
                // và PENDING cho các vòng khác không có luồng nộp bài (AI_INTERVIEW)
                Round nextRound = rounds.get(currentRoundOrder); // index = currentRoundOrder (0-based) = vòng mới
                if (nextRound.getRoundType() == RoundType.MENTROR_REVIEW
                        || nextRound.getRoundType() == RoundType.AI_INTERVIEW) {
                    boolean alreadyExists = applicationDetailRepository
                            .findByApplicationIdAndRoundId(currentApplication.getId(), nextRound.getId())
                            .isPresent();
                    if (!alreadyExists) {
                        ApplicationDetailStatus nextStatus = nextRound.getRoundType() == RoundType.MENTROR_REVIEW
                                ? ApplicationDetailStatus.AWAITING_MENTOR
                                : ApplicationDetailStatus.PENDING;
                        java.time.LocalDateTime endTime = null;
                        if (nextRound.getConfigData() != null
                                && nextRound.getConfigData().getTimeLimitMinutes() != null) {
                            endTime = java.time.LocalDateTime.now()
                                    .plusMinutes(nextRound.getConfigData().getTimeLimitMinutes());
                        }
                        ApplicationDetail nextDetail = ApplicationDetail.builder()
                                .applicationId(currentApplication.getId())
                                .roundId(nextRound.getId())
                                .status(nextStatus)
                                .sessionInfo(ApplicationDetail.RoundSessionInfo.builder()
                                        .startTime(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()))
                                        .endTime(endTime != null ? java.sql.Timestamp.valueOf(endTime) : null)
                                        .build())
                                .build();
                        applicationDetailRepository.save(nextDetail);
                    }
                }

            } else {
                List<ApplicationDetail> details =
                        applicationDetailRepository.findAllByApplicationId(currentApplication.getId());
                double totalEarnedScore = 0;
                double totalMaxScore = 0;
                boolean hasFailedRound = false;

                for (ApplicationDetail detail : details) {
                    if (detail.getFinalScore() != null) {
                        totalEarnedScore += detail.getFinalScore();
                    }
                    if (detail.getFinalResult() == ApplicationDetail.RoundResult.FAILED) {
                        hasFailedRound = true;
                    }

                    final Long detailRoundId = detail.getRoundId();
                    if (detailRoundId != null) {
                        Round matchingRound = rounds.stream()
                                .filter(r -> r.getId() != null && r.getId().equals(detailRoundId))
                                .findFirst()
                                .orElse(null);

                        if (matchingRound != null
                                && matchingRound.getConfigData() != null
                                && matchingRound.getConfigData().getMaxScore() != null) {
                            totalMaxScore += matchingRound.getConfigData().getMaxScore();
                        } else {
                            totalMaxScore += 100.0;
                        }
                    }
                }

                double overallScorePercentage = 0.0;
                if (totalMaxScore > 0) {
                    overallScorePercentage = Math.round((totalEarnedScore / totalMaxScore) * 100.0);
                }
                currentApplication.setOverallScore(overallScorePercentage);

                if (hasFailedRound) {
                    currentApplication.setStatus(ApplicationStatus.FAILED);
                } else {
                    currentApplication.setStatus(ApplicationStatus.PASSED);
                }
                applicationRepository.save(currentApplication);
                System.out.println("Application " + currentApplication.getId() + " finished all rounds. Status: "
                        + currentApplication.getStatus() + ", Overall Score: " + overallScorePercentage + "%");
            }
        }
    }
}
