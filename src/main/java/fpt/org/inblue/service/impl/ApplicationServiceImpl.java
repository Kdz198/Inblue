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
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {
    private final SecurityUtils securityUtils;
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final JdPurchaseRepository jdPurchaseRepository;

    @Override
    public Application applyForJob(Long jdId) {
        int userId = securityUtils.getCurrentUserId();

        JobDescription jd = jobDescriptionRepository
                .findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));

        Optional<JdPurchase> purchaseOpt = jdPurchaseRepository
                .findByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED);

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
    public void moveToNextRound(Application currentApplication) {
        JobDescription jd =
                jobDescriptionRepository.findById(currentApplication.getJdId()).orElse(null);
        System.out.println("Moving application " + currentApplication.getId() + " to next round. Current round order: "
                + currentApplication.getCurrentRoundOrder());
        if (jd != null) {
            List<Round> rounds = jd.getRounds();
            int currentRoundOrder = currentApplication.getCurrentRoundOrder();
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
                System.out.println("Application " + currentApplication.getId() + " moved to round order "
                        + currentApplication.getCurrentRoundOrder());

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
