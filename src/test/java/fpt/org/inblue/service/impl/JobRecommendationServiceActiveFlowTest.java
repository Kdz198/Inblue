package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
import fpt.org.inblue.mapper.JobRecommendationMapper;
import fpt.org.inblue.model.JobRecommendationConfig;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JobRecommendationConfigRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobRecommendationServiceActiveFlowTest {
    @Mock
    UserCareerPreferenceRepository preferenceRepository;

    @Mock
    UserCompetencyRepository competencyRepository;

    @Mock
    JobDescriptionRepository jobDescriptionRepository;

    @Mock
    JobRecommendationConfigRepository configRepository;

    @Mock
    JobRecommendationMapper mapper;

    private JobRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JobRecommendationServiceImpl(
                preferenceRepository, competencyRepository, jobDescriptionRepository, configRepository, mapper);
    }

    @Test
    void recommendationsReturnEmptyWhenCandidateHasNoActivePreference() {
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.empty());

        assertEquals(0, service.getRecommendations(7).size());
    }

    @Test
    void updateThresholdPersistsSingletonConfiguration() {
        JobRecommendationConfig saved = JobRecommendationConfig.builder()
                .id(JobRecommendationConfig.SINGLETON_ID)
                .matchThresholdPercent(new BigDecimal("72.5"))
                .build();
        when(configRepository.findById(JobRecommendationConfig.SINGLETON_ID)).thenReturn(Optional.empty());
        when(configRepository.save(org.mockito.ArgumentMatchers.any(JobRecommendationConfig.class)))
                .thenReturn(saved);

        var response = service.updateThreshold(new BigDecimal("72.5"));

        assertEquals(new BigDecimal("72.5"), response.getThresholdPercent());
        verify(configRepository).save(org.mockito.ArgumentMatchers.any(JobRecommendationConfig.class));
    }

    @Test
    void recommendationsReturnEmptyWhenThresholdConfigIsMissing() {
        UserCareerPreference preference = UserCareerPreference.builder()
                .userId(7)
                .skillEmbedding(new float[] {1.0f, 0.0f})
                .build();
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference));
        when(configRepository.findById(JobRecommendationConfig.SINGLETON_ID)).thenReturn(Optional.empty());
        assertEquals(0, service.getRecommendations(7).size());
    }

    @Test
    void recommendationsReturnEmptyWhenPreferenceHasNoEmbedding() {
        UserCareerPreference preference = UserCareerPreference.builder().userId(7).build();
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference));
        when(configRepository.findById(JobRecommendationConfig.SINGLETON_ID))
                .thenReturn(Optional.of(JobRecommendationConfig.builder()
                        .id(JobRecommendationConfig.SINGLETON_ID)
                        .matchThresholdPercent(BigDecimal.ZERO)
                        .build()));
        assertEquals(0, service.getRecommendations(7).size());
    }
}
