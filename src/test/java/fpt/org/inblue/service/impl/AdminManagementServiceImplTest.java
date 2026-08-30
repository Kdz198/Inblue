//package fpt.org.inblue.service.impl;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import fpt.org.inblue.enums.ApplicationDetailStatus;
//import fpt.org.inblue.enums.ApplicationStatus;
//import fpt.org.inblue.enums.RoundType;
//import fpt.org.inblue.model.dto.response.admin.AdminDashboardOverviewResponse;
//import fpt.org.inblue.model.dto.response.admin.AdminDashboardOverviewResponse.JobTrendItem;
//import fpt.org.inblue.repository.*;
//import fpt.org.inblue.repository.projection.AdminAnalyticsProjection;
//import fpt.org.inblue.service.MentorService;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Pageable;
//
//@ExtendWith(MockitoExtension.class)
//class AdminManagementServiceImplTest {
//
//    @Mock
//    private JobDescriptionRepository jobDescriptionRepository;
//
//    @Mock
//    private CompanyRepository companyRepository;
//
//    @Mock
//    private ApplicationRepository applicationRepository;
//
//    @Mock
//    private ApplicationDetailRepository applicationDetailRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private CandidateProfileRepository candidateProfileRepository;
//
//    @Mock
//    private MentorService mentorService;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private AdminAnalyticsProjection.JobTrend jobTrend;
//
//    @Mock
//    private AdminAnalyticsProjection.PositionTrend positionTrend;
//
//    @Mock
//    private AdminAnalyticsProjection.ApplicationStatusCount inProgressCount;
//
//    @Mock
//    private AdminAnalyticsProjection.ApplicationStatusCount passedCount;
//
//    @Mock
//    private AdminAnalyticsProjection.ApplicationStatusCount failedCount;
//
//    @Mock
//    private AdminAnalyticsProjection.ActiveInterview activeInterview;
//
//    private AdminManagementServiceImpl service;
//    private RoundRepository roundRepository;
//
//    @BeforeEach
//    void setUp() {
//        service = new AdminManagementServiceImpl(
//                jobDescriptionRepository,
//                companyRepository,
//                applicationRepository,
//                applicationDetailRepository,
//                userRepository,
//                candidateProfileRepository,
//                mentorService,
//                paymentRepository,
//                roundRepository);
//    }
//
//    @Test
//    void dashboardOverviewAggregatesAndMapsAnalyticsForUi() {
//        when(jobTrend.getJobId()).thenReturn(46L);
//        when(jobTrend.getJobTitle()).thenReturn("Backend Engineer");
//        when(jobTrend.getApplicationCount()).thenReturn(6L);
//
//        when(positionTrend.getPosition()).thenReturn("Java Developer");
//        when(positionTrend.getApplicationCount()).thenReturn(4L);
//
//        when(inProgressCount.getStatus()).thenReturn(ApplicationStatus.IN_PROGRESS);
//        when(inProgressCount.getApplicationCount()).thenReturn(5L);
//        when(passedCount.getStatus()).thenReturn(ApplicationStatus.PASSED);
//        when(passedCount.getApplicationCount()).thenReturn(3L);
//        when(failedCount.getStatus()).thenReturn(ApplicationStatus.FAILED);
//        when(failedCount.getApplicationCount()).thenReturn(2L);
//
//        when(activeInterview.getApplicationDetailId()).thenReturn(501L);
//        when(activeInterview.getApplicationId()).thenReturn(601L);
//        when(activeInterview.getUserId()).thenReturn(7);
//        when(activeInterview.getUserName()).thenReturn("Candidate");
//        when(activeInterview.getUserEmail()).thenReturn("candidate@example.com");
//        when(activeInterview.getJobId()).thenReturn(46L);
//        when(activeInterview.getJobTitle()).thenReturn("Backend Engineer");
//        when(activeInterview.getRoundId()).thenReturn(101L);
//        when(activeInterview.getRoundOrder()).thenReturn(2);
//        when(activeInterview.getRoundName()).thenReturn("AI Interview");
//        when(activeInterview.getRoundType()).thenReturn(RoundType.AI_INTERVIEW);
//        when(activeInterview.getRoundStatus()).thenReturn(ApplicationDetailStatus.PENDING);
//
//        when(applicationRepository.findApplicationTrendsByJob(any(Pageable.class)))
//                .thenReturn(List.of(jobTrend));
//        when(candidateProfileRepository.findApplicationTrendsByPosition(any(Pageable.class)))
//                .thenReturn(List.of(positionTrend));
//        when(applicationRepository.countApplicationsByStatus())
//                .thenReturn(List.of(inProgressCount, passedCount, failedCount));
//        when(applicationDetailRepository.findActiveInterviews(any(), any(), any()))
//                .thenReturn(List.of(activeInterview));
//        when(applicationDetailRepository.countActiveInterviews(any(), any())).thenReturn(2L);
//        when(paymentRepository.findRecentTransactions(any(), any(), any(), any()))
//                .thenReturn(List.of());
//
//        AdminDashboardOverviewResponse response = service.getDashboardOverview(10);
//
//        assertEquals(10L, response.getSummary().getTotalApplications());
//        assertEquals(5L, response.getSummary().getInProgressApplications());
//        assertEquals(3L, response.getSummary().getPassedApplications());
//        assertEquals(2L, response.getSummary().getFailedApplications());
//        assertEquals(2L, response.getSummary().getActiveInterviewCount());
//
//        JobTrendItem topJob = response.getJobTrends().get(0);
//        assertEquals(1, topJob.getRank());
//        assertEquals(6L, topJob.getApplicationCount());
//        assertEquals(60.0, topJob.getPercentage());
//        assertEquals(40.0, response.getPositionTrends().get(0).getPercentage());
//        assertEquals(1, response.getActiveInterviews().size());
//        assertEquals("AI Interview", response.getActiveInterviews().get(0).getRoundName());
//        assertEquals(601L, response.getActiveInterviews().get(0).getApplicationId());
//
//        verify(applicationRepository).findApplicationTrendsByJob(any(Pageable.class));
//        verify(candidateProfileRepository).findApplicationTrendsByPosition(any(Pageable.class));
//    }
//}
