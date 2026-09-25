package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundServiceImplActiveFlowTest {
    @Mock
    RoundRepository repository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    CodingProblemsRepository codingRepository;

    @Mock
    ApplicationService applicationService;

    @Mock
    JobDescriptionService jobService;

    @Mock
    CodeReviewProblemsRepository reviewRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    ApiClient apiClient;

    private RoundServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoundServiceImpl(
                repository,
                jobRepository,
                codingRepository,
                applicationService,
                jobService,
                reviewRepository,
                companyRepository,
                apiClient);
    }

    @Test
    void getRoundByIdRejectsUnknownRound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getRoundById(9L))
                        .getStatus()
                        .value());
    }

    @Test
    void getAllRoundTypesExposesConfiguredEnumValues() {
        assertEquals(
                fpt.org.inblue.enums.RoundType.values().length,
                service.getAllRoundTypes().size());
    }

    @Test
    void getRoundByOrderUsesApplicationCurrentOrder() {
        Application app =
                Application.builder().id(1L).jdId(2L).currentRoundOrder(3).build();
        Round round = Round.builder().id(4L).build();
        when(applicationService.getApplicationById(1L)).thenReturn(app);
        when(jobService.getRoundByOrder(2L, 3)).thenReturn(round);
        assertEquals(round, service.getRoundByOrder(1L));
    }
}
