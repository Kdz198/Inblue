package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.AdminJdApplicationsResponseDto;
import fpt.org.inblue.model.dto.response.admin.*;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.AdminManagementService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminOpenJdResponseDto> getOpenJdsWithCompanyAndStats(JobDescriptionStatus status) {
        List<JobDescription> jds = (status != null)
                ? jobDescriptionRepository.findByStatusAndIsDeletedFalse(status)
                : jobDescriptionRepository.findByIsDeletedFalse();

        List<AdminOpenJdResponseDto> result = new ArrayList<>();

        for (JobDescription jd : jds) {
            Optional<Company> companyOpt = companyRepository.findByJobDescriptionsId(jd.getId());
            AdminOpenJdResponseDto.CompanySummaryDto companyDto = companyOpt.map(c -> AdminOpenJdResponseDto.CompanySummaryDto.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .logoUrl(c.getLogoUrl())
                    .bannerUrl(c.getBannerUrl())
                    .status(c.getStatus())
                    .build()).orElse(null);

            List<Application> apps = applicationRepository.findByJdIdAndIsDeletedFalse(jd.getId());

            int inProgress = 0;
            int passed = 0;
            int failed = 0;

            for (Application app : apps) {
                if (app.getStatus() == ApplicationStatus.IN_PROGRESS) {
                    inProgress++;
                } else if (app.getStatus() == ApplicationStatus.PASSED) {
                    passed++;
                } else if (app.getStatus() == ApplicationStatus.FAILED || app.getStatus() == ApplicationStatus.SOFT_FAILED) {
                    failed++;
                }
            }

            AdminOpenJdResponseDto.ApplicationStatisticsDto statsDto = AdminOpenJdResponseDto.ApplicationStatisticsDto.builder()
                    .totalApplications(apps.size())
                    .inProgressCount(inProgress)
                    .passedCount(passed)
                    .failedCount(failed)
                    .build();

            AdminOpenJdResponseDto dto = AdminOpenJdResponseDto.builder()
                    .jdId(jd.getId())
                    .title(jd.getTitle())
                    .description(jd.getDescription())
                    .requirements(jd.getRequirements())
                    .benefits(jd.getBenefits())
                    .level(jd.getLevel())
                    .salaryMin(jd.getSalaryMin())
                    .salaryMax(jd.getSalaryMax())
                    .currency(jd.getCurrency())
                    .price(jd.getPrice())
                    .status(jd.getStatus())
                    .roundsCount(jd.getRounds() != null ? jd.getRounds().size() : 0)
                    .deadlineAt(jd.getDeadlineAt())
                    .createdAt(jd.getCreatedAt())
                    .updatedAt(jd.getUpdatedAt())
                    .company(companyDto)
                    .statistics(statsDto)
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminJdApplicationsResponseDto getApplicationsByJdId(Long jdId) {
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc với ID: " + jdId, HttpStatus.NOT_FOUND));

        Optional<Company> companyOpt = companyRepository.findByJobDescriptionsId(jdId);
        String companyName = companyOpt.map(Company::getName).orElse("N/A");
        String companyLogo = companyOpt.map(Company::getLogoUrl).orElse(null);

        List<Application> apps = applicationRepository.findByJdIdAndIsDeletedFalse(jdId);

        List<AdminApplicationSummaryDto> appSummaries = new ArrayList<>();
        int inProgress = 0;
        int passed = 0;
        int failed = 0;
        double scoreSum = 0.0;
        int validScoreCount = 0;

        for (Application app : apps) {
            if (app.getStatus() == ApplicationStatus.IN_PROGRESS) {
                inProgress++;
            } else if (app.getStatus() == ApplicationStatus.PASSED) {
                passed++;
            } else if (app.getStatus() == ApplicationStatus.FAILED || app.getStatus() == ApplicationStatus.SOFT_FAILED) {
                failed++;
            }

            if (app.getOverallScore() != null && app.getOverallScore() >= 0) {
                scoreSum += app.getOverallScore();
                validScoreCount++;
            }

            Optional<User> userOpt = userRepository.findById(app.getUserId());
            CandidateProfile candidateProfile = candidateProfileRepository.findByUser_Id(app.getUserId());

            String candidateName = userOpt.map(User::getName).orElse("N/A");
            String candidateEmail = userOpt.map(User::getEmail).orElse("N/A");
            String avatarUrl = userOpt.map(User::getAvatarUrl).orElse(null);
            String targetRole = candidateProfile != null ? candidateProfile.getTargetRole() : null;
            String targetLevel = candidateProfile != null ? candidateProfile.getTargetLevel() : null;

            String currentRoundName = "Unknown Round";
            if (jd.getRounds() != null && app.getCurrentRoundOrder() != null) {
                currentRoundName = jd.getRounds().stream()
                        .filter(r -> r.getRoundOrder() != null && r.getRoundOrder().equals(app.getCurrentRoundOrder()))
                        .map(Round::getName)
                        .findFirst()
                        .orElse("Vòng " + app.getCurrentRoundOrder());
            }

            AdminApplicationSummaryDto appDto = AdminApplicationSummaryDto.builder()
                    .applicationId(app.getId())
                    .userId(app.getUserId())
                    .candidateName(candidateName)
                    .candidateEmail(candidateEmail)
                    .avatarUrl(avatarUrl)
                    .targetRole(targetRole)
                    .targetLevel(targetLevel)
                    .status(app.getStatus())
                    .overallScore(app.getOverallScore())
                    .currentRoundOrder(app.getCurrentRoundOrder())
                    .currentRoundName(currentRoundName)
                    .appliedAt(app.getCreatedAt())
                    .updatedAt(app.getUpdatedAt())
                    .build();

            appSummaries.add(appDto);
        }

        AdminJdApplicationsResponseDto.JdSummaryDto jdSummaryDto = AdminJdApplicationsResponseDto.JdSummaryDto.builder()
                .jdId(jd.getId())
                .jdTitle(jd.getTitle())
                .companyName(companyName)
                .companyLogo(companyLogo)
                .totalRounds(jd.getRounds() != null ? jd.getRounds().size() : 0)
                .build();

        AdminJdApplicationsResponseDto.SummaryStatisticsDto statsDto = AdminJdApplicationsResponseDto.SummaryStatisticsDto.builder()
                .totalApplications(apps.size())
                .inProgressCount(inProgress)
                .passedCount(passed)
                .failedCount(failed)
                .avgOverallScore(validScoreCount > 0 ? Math.round((scoreSum / validScoreCount) * 100.0) / 100.0 : 0.0)
                .build();

        return AdminJdApplicationsResponseDto.builder()
                .jdInfo(jdSummaryDto)
                .summaryStatistics(statsDto)
                .applications(appSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApplicationFullDetailResponseDto getApplicationFullDetail(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Không tìm thấy đơn ứng tuyển với ID: " + applicationId, HttpStatus.NOT_FOUND));

        JobDescription jd = jobDescriptionRepository.findById(application.getJdId())
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc của đơn ứng tuyển này", HttpStatus.NOT_FOUND));

        Optional<Company> companyOpt = companyRepository.findByJobDescriptionsId(jd.getId());
        String companyName = companyOpt.map(Company::getName).orElse("N/A");
        String companyLogo = companyOpt.map(Company::getLogoUrl).orElse(null);

        Optional<User> userOpt = userRepository.findById(application.getUserId());
        CandidateProfile candidateProfile = candidateProfileRepository.findByUser_Id(application.getUserId());

        String currentRoundName = "Vòng " + application.getCurrentRoundOrder();
        if (jd.getRounds() != null && application.getCurrentRoundOrder() != null) {
            currentRoundName = jd.getRounds().stream()
                    .filter(r -> r.getRoundOrder() != null && r.getRoundOrder().equals(application.getCurrentRoundOrder()))
                    .map(Round::getName)
                    .findFirst()
                    .orElse(currentRoundName);
        }

        AdminApplicationFullDetailResponseDto.ApplicationOverviewDto appOverview = AdminApplicationFullDetailResponseDto.ApplicationOverviewDto.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .overallScore(application.getOverallScore())
                .currentRoundOrder(application.getCurrentRoundOrder())
                .currentRoundName(currentRoundName)
                .totalRounds(jd.getRounds() != null ? jd.getRounds().size() : 0)
                .appliedAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();

        AdminApplicationFullDetailResponseDto.JobDescriptionInfoDto jdInfo = AdminApplicationFullDetailResponseDto.JobDescriptionInfoDto.builder()
                .jdId(jd.getId())
                .title(jd.getTitle())
                .level(jd.getLevel() != null ? jd.getLevel().name() : null)
                .salaryMin(jd.getSalaryMin())
                .salaryMax(jd.getSalaryMax())
                .currency(jd.getCurrency())
                .companyId(companyOpt.map(Company::getId).orElse(null))
                .companyName(companyName)
                .companyLogo(companyLogo)
                .build();

        AdminApplicationFullDetailResponseDto.CandidateInfoDto candidateInfo = AdminApplicationFullDetailResponseDto.CandidateInfoDto.builder()
                .userId(application.getUserId())
                .name(userOpt.map(User::getName).orElse("N/A"))
                .email(userOpt.map(User::getEmail).orElse("N/A"))
                .avatarUrl(userOpt.map(User::getAvatarUrl).orElse(null))
                .cvUrl(userOpt.map(User::getCvUrl).orElse(null))
                .profile(candidateProfile)
                .build();

        List<ApplicationDetail> appDetails = applicationDetailRepository.findAllByApplicationId(applicationId);

        List<AdminRoundDetailDto> roundDetails = new ArrayList<>();

        for (ApplicationDetail detail : appDetails) {
            Optional<Round> roundOpt = Optional.empty();
            if (jd.getRounds() != null && detail.getRoundId() != null) {
                roundOpt = jd.getRounds().stream()
                        .filter(r -> detail.getRoundId().equals(r.getId()))
                        .findFirst();
            }

            AdminRoundDetailDto roundDto = AdminRoundDetailDto.builder()
                    .applicationDetailId(detail.getId())
                    .roundId(detail.getRoundId())
                    .roundOrder(roundOpt.map(Round::getRoundOrder).orElse(null))
                    .roundName(roundOpt.map(Round::getName).orElse("Unknown Round"))
                    .roundType(roundOpt.map(Round::getRoundType).orElse(null))
                    .passThreshold(roundOpt.map(Round::getPassThreshold).orElse(null))
                    .reviewerId(roundOpt.map(Round::getReviewerId).orElse(null))
                    .roundConfig(roundOpt.map(Round::getConfigData).orElse(null))
                    .status(detail.getStatus())
                    .aiScore(detail.getAiScore())
                    .aiFeedback(detail.getAiFeedback())
                    .hrScore(detail.getHrScore())
                    .hrNote(detail.getHrNote())
                    .finalScore(detail.getFinalScore())
                    .finalResult(detail.getFinalResult())
                    .submissionData(detail.getSubmissionData())
                    .sessionInfo(detail.getSessionInfo())
                    .mentorId(detail.getMentorId())
                    .mentorReview(detail.getMentorReview())
                    .startedAt(detail.getStartedAt())
                    .completedAt(detail.getCompletedAt())
                    .createdAt(detail.getCreatedAt())
                    .updatedAt(detail.getUpdatedAt())
                    .build();

            roundDetails.add(roundDto);
        }

        // Sort by round order
        roundDetails.sort(Comparator.comparing(
                AdminRoundDetailDto::getRoundOrder,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return AdminApplicationFullDetailResponseDto.builder()
                .applicationOverview(appOverview)
                .jobDescriptionInfo(jdInfo)
                .candidateInfo(candidateInfo)
                .roundDetails(roundDetails)
                .build();
    }
}
