package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.JourneySummaryService;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.utils.SecurityUtils;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceActiveFlowTest {
    @Mock
    SecurityUtils securityUtils;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    JobDescriptionRepository jobDescriptionRepository;

    @Mock
    ApplicationDetailRepository applicationDetailRepository;

    @Mock
    JdPurchaseRepository jdPurchaseRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    JourneySummaryService journeySummaryService;

    @Mock
    JourneySummaryServiceImpl journeySummaryServiceImpl;

    @Mock
    ObjectProvider<ApplicationDetailService> applicationDetailServiceProvider;

    private ApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(
                securityUtils,
                applicationRepository,
                jobDescriptionRepository,
                applicationDetailRepository,
                jdPurchaseRepository,
                userRepository,
                eventPublisher,
                journeySummaryService,
                journeySummaryServiceImpl,
                applicationDetailServiceProvider);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(7);
        lenient().when(applicationRepository.save(any(Application.class))).thenAnswer(i -> {
            Application app = i.getArgument(0);
            app.setId(55L);
            return app;
        });
    }

    @Test
    void applyForJobRejectsMissingJd() {
        when(jobDescriptionRepository.findById(99L)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.applyForJob(99L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void applyForPaidJdRequiresPurchasedPackage() {
        JobDescription jd =
                JobDescription.builder().id(10L).price(100L).appliedCount(0).build();
        when(jobDescriptionRepository.findById(10L)).thenReturn(Optional.of(jd));
        when(jdPurchaseRepository.findByUserIdAndJdIdAndStatus(7, 10L, JdPurchaseStatus.PURCHASED))
                .thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.applyForJob(10L));
        assertEquals(HttpStatus.PAYMENT_REQUIRED, error.getStatus());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void applyForFreeJdCreatesUsedPurchaseAndApplication() {
        JobDescription jd =
                JobDescription.builder().id(10L).price(0L).appliedCount(2).build();
        when(jobDescriptionRepository.findById(10L)).thenReturn(Optional.of(jd));
        when(jdPurchaseRepository.findByUserIdAndJdIdAndStatus(7, 10L, JdPurchaseStatus.PURCHASED))
                .thenReturn(Optional.empty());
        Application app = service.applyForJob(10L);
        assertEquals(7, app.getUserId());
        assertEquals(10L, app.getJdId());
        assertEquals(3, jd.getAppliedCount());
        verify(jdPurchaseRepository).save(any(JdPurchase.class));
    }

    @Test
    void applyForPurchasedJdConsumesPurchase() {
        JobDescription jd =
                JobDescription.builder().id(10L).price(100L).appliedCount(0).build();
        JdPurchase purchase =
                JdPurchase.builder().status(JdPurchaseStatus.PURCHASED).build();
        when(jobDescriptionRepository.findById(10L)).thenReturn(Optional.of(jd));
        when(jdPurchaseRepository.findByUserIdAndJdIdAndStatus(7, 10L, JdPurchaseStatus.PURCHASED))
                .thenReturn(Optional.of(purchase));
        service.applyForJob(10L);
        assertEquals(JdPurchaseStatus.USED, purchase.getStatus());
        verify(jdPurchaseRepository).save(purchase);
    }

    @Test
    void applyForAiInterviewRoundCreatesPendingDetail() {
        Round round = Round.builder()
                .id(3L)
                .roundOrder(1)
                .roundType(RoundType.AI_INTERVIEW)
                .build();
        JobDescription jd = JobDescription.builder()
                .id(10L)
                .price(0L)
                .appliedCount(0)
                .rounds(List.of(round))
                .build();
        when(jobDescriptionRepository.findById(10L)).thenReturn(Optional.of(jd));
        when(jdPurchaseRepository.findByUserIdAndJdIdAndStatus(7, 10L, JdPurchaseStatus.PURCHASED))
                .thenReturn(Optional.empty());
        service.applyForJob(10L);
        verify(applicationDetailRepository).save(any(ApplicationDetail.class));
    }

    @Test
    void getApplicationByIdRejectsUnknownApplication() {
        when(applicationRepository.findById(404L)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.getApplicationById(404L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void applicationLookupRejectsBlankEmail() {
        CustomException error = assertThrows(CustomException.class, () -> service.getAllApplicationsByUserEmail(" "));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void moveToNextRoundMarksSoftFailedWhenExistingDetailFailed() {
        Round first =
                Round.builder().id(1L).roundOrder(1).roundType(RoundType.CODING).build();
        Round second = Round.builder()
                .id(2L)
                .roundOrder(2)
                .roundType(RoundType.AI_INTERVIEW)
                .build();
        JobDescription jd =
                JobDescription.builder().id(10L).rounds(List.of(first, second)).build();
        Application app = new Application();
        app.setId(5L);
        app.setJdId(10L);
        app.setCurrentRoundOrder(1);
        app.setStatus(ApplicationStatus.IN_PROGRESS);
        ApplicationDetail failed = new ApplicationDetail();
        failed.setFinalResult(ApplicationDetail.RoundResult.FAILED);
        when(jobDescriptionRepository.findById(10L)).thenReturn(Optional.of(jd));
        when(applicationDetailRepository.findAllByApplicationId(5L)).thenReturn(List.of(failed));
        when(applicationDetailRepository.findByApplicationIdAndRoundId(55L, 2L)).thenReturn(Optional.empty());
        service.moveToNextRound(app);
        assertEquals(ApplicationStatus.SOFT_FAILED, app.getStatus());
        assertEquals(2, app.getCurrentRoundOrder());
        verify(applicationRepository).save(app);
    }
}
