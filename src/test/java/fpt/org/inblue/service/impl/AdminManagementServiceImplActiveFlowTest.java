package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.CandidateProfileRepository;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.MentorService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminManagementServiceImplActiveFlowTest {
    @Mock
    JobDescriptionRepository jobDescriptionRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationDetailRepository applicationDetailRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CandidateProfileRepository candidateProfileRepository;

    @Mock
    MentorService mentorService;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    RoundRepository roundRepository;

    private AdminManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminManagementServiceImpl(
                jobDescriptionRepository,
                companyRepository,
                applicationRepository,
                applicationDetailRepository,
                userRepository,
                candidateProfileRepository,
                mentorService,
                paymentRepository,
                roundRepository);
    }

    @Test
    void openJdStatsGroupSoftFailedApplicationsAsFailed() {
        JobDescription jd = JobDescription.builder()
                .id(10L)
                .title("Backend Engineer")
                .status(JobDescriptionStatus.OPEN)
                .build();
        Application passed = new Application();
        passed.setStatus(ApplicationStatus.PASSED);
        Application softFailed = new Application();
        softFailed.setStatus(ApplicationStatus.SOFT_FAILED);
        when(jobDescriptionRepository.findByStatusAndIsDeletedFalse(JobDescriptionStatus.OPEN))
                .thenReturn(List.of(jd));
        when(companyRepository.findByJobDescriptionsId(10L)).thenReturn(Optional.empty());
        when(applicationRepository.findByJdIdAndIsDeletedFalse(10L)).thenReturn(List.of(passed, softFailed));

        var response = service.getOpenJdsWithCompanyAndStats(JobDescriptionStatus.OPEN);

        assertEquals(1, response.size());
        assertEquals(2, response.getFirst().getStatistics().getTotalApplications());
        assertEquals(1, response.getFirst().getStatistics().getPassedCount());
        assertEquals(1, response.getFirst().getStatistics().getFailedCount());
    }
}
