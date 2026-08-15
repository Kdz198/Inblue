package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.enums.JdPurchaseStatus;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.JdPurchase;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.response.ApplicationLookupResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JourneySummaryService;
import fpt.org.inblue.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    private final SecurityUtils securityUtils;
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final JdPurchaseRepository jdPurchaseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JourneySummaryService journeySummaryService;
    private final JourneySummaryServiceImpl journeySummaryServiceImpl;

    @Override
    public Application applyForJob(Long jdId) {
        int userId = securityUtils.getCurrentUserId();

        JobDescription jd = jobDescriptionRepository
                .findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));

        Optional<JdPurchase> purchaseOpt =
                jdPurchaseRepository.findByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED);

        if (purchaseOpt.isPresent()) {
            JdPurchase purchase = purchaseOpt.get();
            purchase.setStatus(JdPurchaseStatus.USED);
            purchase.setUsedAt(LocalDateTime.now());
            jdPurchaseRepository.save(purchase);
        } else if (jd.getPrice() == null || jd.getPrice() <= 0) {
            JdPurchase freePurchase = JdPurchase.builder()
                    .userId(userId)
                    .jdId(jdId)
                    .paymentId(0)
                    .status(JdPurchaseStatus.USED)
                    .purchasedAt(LocalDateTime.now())
                    .usedAt(LocalDateTime.now())
                    .build();
            jdPurchaseRepository.save(freePurchase);
        } else {
            throw new CustomException("Bạn cần mua gói apply cho JD này trước", HttpStatus.PAYMENT_REQUIRED);
        }

        Application application = new Application();
        application.setUserId(userId);
        application.setJdId(jdId);
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
                ApplicationDetail.RoundSessionInfo sessionInfo = null;
                if (initialStatus == ApplicationDetailStatus.PENDING) {
                    java.time.LocalDateTime endTime = null;
                    if (firstRound.getConfigData() != null
                            && firstRound.getConfigData().getTimeLimitMinutes() != null) {
                        endTime = java.time.LocalDateTime.now()
                                .plusMinutes(firstRound.getConfigData().getTimeLimitMinutes());
                    }
                    sessionInfo = ApplicationDetail.RoundSessionInfo.builder()
                            .startTime(java.time.LocalDateTime.now())
                            .endTime(endTime)
                            .build();
                } else {
                    sessionInfo = new ApplicationDetail.RoundSessionInfo();
                }
                ApplicationDetail firstDetail = ApplicationDetail.builder()
                        .applicationId(savedApplication.getId())
                        .roundId(firstRound.getId())
                        .status(initialStatus)
                        .sessionInfo(sessionInfo)
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
        int userId = securityUtils.getCurrentUserId();
        return applicationRepository.findAllByUserId(userId);
    }

    @Override
    public List<ApplicationLookupResponse> getAllApplicationsByUserEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException("Email is required", HttpStatus.BAD_REQUEST);
        }

        var user = userRepository.findByEmail(email.trim());
        if (user == null) {
            throw new CustomException("User not found with email: " + email.trim(), HttpStatus.NOT_FOUND);
        }

        List<Application> applications =
                applicationRepository.findAllByUserIdAndStatusNot(user.getId(), ApplicationStatus.IN_PROGRESS);
        Map<Long, String> applicationNamesByJdId = jobDescriptionRepository
                .findAllById(applications.stream()
                        .map(Application::getJdId)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(JobDescription::getId, JobDescription::getTitle));

        return applications.stream()
                .map(application -> ApplicationLookupResponse.builder()
                        .id(application.getId())
                        .userId(application.getUserId())
                        .jdId(application.getJdId())
                        .applicationName(applicationNamesByJdId.get(application.getJdId()))
                        .currentRoundOrder(application.getCurrentRoundOrder())
                        .status(application.getStatus())
                        .overallScore(application.getOverallScore())
                        .isDeleted(application.getIsDeleted())
                        .createdAt(application.getCreatedAt())
                        .updatedAt(application.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public void moveToNextRound(Application currentApplication) {
        JobDescription jd =
                jobDescriptionRepository.findById(currentApplication.getJdId()).orElse(null);
        log.info(
                "Moving applicationId={} to next round. currentRoundOrder={}",
                currentApplication.getId(),
                currentApplication.getCurrentRoundOrder());
        if (jd != null) {
            List<Round> rounds = jd.getRounds();
            int currentRoundOrder = currentApplication.getCurrentRoundOrder();
            log.info(
                    "Application round progress check: applicationId={}, currentRoundOrder={}, totalRounds={}",
                    currentApplication.getId(),
                    currentRoundOrder,
                    rounds != null ? rounds.size() : 0);
            if (currentRoundOrder < rounds.size()) {
                List<ApplicationDetail> existingDetails =
                        applicationDetailRepository.findAllByApplicationId(currentApplication.getId());
                boolean hasFailed = existingDetails.stream()
                        .anyMatch(d -> d.getFinalResult() == ApplicationDetail.RoundResult.FAILED);
                if (hasFailed) {
                    currentApplication.setStatus(ApplicationStatus.SOFT_FAILED);
                }

                int nextRoundOrder = currentRoundOrder + 1;
                currentApplication.setCurrentRoundOrder(nextRoundOrder);
                applicationRepository.save(currentApplication);
                log.info(
                        "Application moved to next round: applicationId={}, nextRoundOrder={}",
                        currentApplication.getId(),
                        currentApplication.getCurrentRoundOrder());

                // Tự động tạo ApplicationDetail với status AWAITING_MENTOR cho vòng MENTROR_REVIEW
                // và PENDING cho các vòng khác không có luồng nộp bài (AI_INTERVIEW)
                Round nextRound = rounds.stream()
                        .filter(r -> r.getRoundOrder() != null && r.getRoundOrder() == nextRoundOrder)
                        .findFirst()
                        .orElse(null);
                if (nextRound != null
                        && (nextRound.getRoundType() == RoundType.MENTROR_REVIEW
                                || nextRound.getRoundType() == RoundType.AI_INTERVIEW)) {
                    boolean alreadyExists = applicationDetailRepository
                            .findByApplicationIdAndRoundId(currentApplication.getId(), nextRound.getId())
                            .isPresent();
                    if (!alreadyExists) {
                        ApplicationDetailStatus nextStatus = nextRound.getRoundType() == RoundType.MENTROR_REVIEW
                                ? ApplicationDetailStatus.AWAITING_MENTOR
                                : ApplicationDetailStatus.PENDING;
                        ApplicationDetail.RoundSessionInfo sessionInfo = null;
                        if (nextStatus == ApplicationDetailStatus.PENDING) {
                            java.time.LocalDateTime endTime = null;
                            if (nextRound.getConfigData() != null
                                    && nextRound.getConfigData().getTimeLimitMinutes() != null) {
                                endTime = java.time.LocalDateTime.now()
                                        .plusMinutes(nextRound.getConfigData().getTimeLimitMinutes());
                            }
                            sessionInfo = ApplicationDetail.RoundSessionInfo.builder()
                                    .startTime(java.time.LocalDateTime.now())
                                    .endTime(endTime)
                                    .build();
                        } else {
                            sessionInfo = new ApplicationDetail.RoundSessionInfo();
                        }
                        ApplicationDetail nextDetail = ApplicationDetail.builder()
                                .applicationId(currentApplication.getId())
                                .roundId(nextRound.getId())
                                .status(nextStatus)
                                .sessionInfo(sessionInfo)
                                .build();
                        applicationDetailRepository.save(nextDetail);
                    }
                }

            } else {
                boolean wasFinished = currentApplication.getStatus() == ApplicationStatus.PASSED
                        || currentApplication.getStatus() == ApplicationStatus.FAILED;
                List<ApplicationDetail> details =
                        applicationDetailRepository.findAllByApplicationId(currentApplication.getId());
                double totalEarnedScore = 0;
                double totalMaxScore = 0;
                boolean hasFailedRound = false;

                for (ApplicationDetail detail : details) {
                    Double roundScore = detail.getFinalScore();
                    if (roundScore == null) {
                        roundScore = detail.getAiScore();
                        if (roundScore != null && roundScore <= 10.0) {
                            roundScore = roundScore * 10.0;
                        }
                    }
                    if (roundScore == null) {
                        roundScore = detail.getHrScore();
                    }

                    if (roundScore != null) {
                        totalEarnedScore += Math.min(100.0, Math.max(0.0, roundScore));
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
                    overallScorePercentage = Math.min(100.0, Math.max(0.0, overallScorePercentage));
                }
                currentApplication.setOverallScore(overallScorePercentage);

                if (hasFailedRound) {
                    currentApplication.setStatus(ApplicationStatus.FAILED);
                } else {
                    currentApplication.setStatus(ApplicationStatus.PASSED);
                }
                applicationRepository.save(currentApplication);
                boolean allRoundsCompleted = areAllRoundsCompleted(rounds, details);
                log.info(
                        "Final round completion check: applicationId={}, wasFinished={}, allRoundsCompleted={}, finalStatus={}, detailCount={}",
                        currentApplication.getId(),
                        wasFinished,
                        allRoundsCompleted,
                        currentApplication.getStatus(),
                        details.size());
                if (!wasFinished && allRoundsCompleted) {
                    log.info("Publishing AllRoundsCompletedEvent for applicationId={}", currentApplication.getId());
                    journeySummaryServiceImpl.generate(currentApplication.getId());
                } else {
                    log.info(
                            "Skip publishing AllRoundsCompletedEvent for applicationId={}. wasFinished={}, allRoundsCompleted={}",
                            currentApplication.getId(),
                            wasFinished,
                            allRoundsCompleted);
                }
                log.info(
                        "Application finished all rounds: applicationId={}, status={}, overallScore={}",
                        currentApplication.getId(),
                        currentApplication.getStatus(),
                        overallScorePercentage);
            }
        }
    }

    private boolean areAllRoundsCompleted(List<Round> rounds, List<ApplicationDetail> details) {
        if (rounds == null || rounds.isEmpty()) {
            return false;
        }
        return rounds.stream().allMatch(round -> details.stream()
                .anyMatch(detail -> detail.getRoundId() != null
                        && round.getId() != null
                        && detail.getRoundId().equals(round.getId())
                        && detail.getFinalResult() != null));
    }
}
