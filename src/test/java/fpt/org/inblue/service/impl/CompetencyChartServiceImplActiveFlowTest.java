package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetencyChartServiceImplActiveFlowTest {
    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    RoundRepository roundRepository;

    @Mock
    UserRepository userRepository;

    private CompetencyChartServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CompetencyChartServiceImpl(
                applicationRepository, detailRepository, jobRepository, roundRepository, userRepository);
    }

    @Test
    void chartRejectsUnknownApplication() {
        when(applicationRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getCompetencyChart(9L))
                        .getStatus()
                        .value());
    }

    @Test
    void chartMapsMetadataForApplicationWithoutRoundScores() {
        Application app = Application.builder().id(1L).jdId(2L).userId(3).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobRepository.findById(2L))
                .thenReturn(Optional.of(
                        JobDescription.builder().id(2L).title("Backend").build()));
        when(userRepository.findById(3))
                .thenReturn(Optional.of(User.builder().id(3).name("Candidate").build()));
        when(detailRepository.findAllByApplicationId(1L)).thenReturn(List.of());
        when(roundRepository.findAllById(List.of())).thenReturn(List.of());
        var chart = service.getCompetencyChart(1L);
        assertEquals("Candidate", chart.getCandidateName());
        assertEquals("Backend", chart.getJobTitle());
    }
}
