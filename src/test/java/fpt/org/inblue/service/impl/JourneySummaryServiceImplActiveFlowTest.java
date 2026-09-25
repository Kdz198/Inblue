package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.model.dto.response.CompetencyChartResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JourneySummaryRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.CompetencyChartService;
import fpt.org.inblue.service.summary.RoundSummaryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JourneySummaryServiceImplActiveFlowTest {
    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    RoundRepository roundRepository;

    @Mock
    JourneySummaryRepository summaryRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RoundSummaryService roundSummaryService;

    @Mock
    CompetencyChartService competencyChartService;

    @Mock
    ApiClient apiClient;

    private JourneySummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JourneySummaryServiceImpl(
                applicationRepository,
                jobRepository,
                detailRepository,
                roundRepository,
                summaryRepository,
                userRepository,
                roundSummaryService,
                competencyChartService,
                apiClient);
    }

    @Test
    void saveNarrativeRejectsBlankText() {
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.saveNarrative(1L, " "))
                        .getStatus()
                        .value());
    }

    @Test
    void saveNarrativeTrimsAndPersistsText() {
        when(summaryRepository.save(org.mockito.ArgumentMatchers.any(JourneySummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("Summary", service.saveNarrative(1L, "  Summary  ").getNarrative());
    }

    @Test
    void getSavedSummaryRejectsUnavailableReport() {
        when(summaryRepository.findTopByApplicationIdOrderByGeneratedAtDesc(1L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getSavedSummary(1L))
                        .getStatus()
                        .value());
    }

    @Test
    void getSavedCompetencyChartRejectsSummaryWithoutChart() {
        when(summaryRepository.findTopByApplicationIdOrderByGeneratedAtDesc(1L))
                .thenReturn(Optional.of(JourneySummary.builder().build()));
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getSavedCompetencyChart(1L))
                        .getStatus()
                        .value());
    }

    @Test
    void getSavedCompetencyChartReturnsStoredChart() {
        CompetencyChartResponse chart = new CompetencyChartResponse();
        when(summaryRepository.findTopByApplicationIdOrderByGeneratedAtDesc(1L))
                .thenReturn(Optional.of(
                        JourneySummary.builder().competencyChart(chart).build()));
        assertSame(chart, service.getSavedCompetencyChart(1L));
    }
}
