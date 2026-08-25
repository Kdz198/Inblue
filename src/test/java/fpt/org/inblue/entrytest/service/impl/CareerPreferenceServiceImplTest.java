package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CareerPreferenceServiceImplTest {
    private static final Integer USER_ID = 1;

    @Mock
    private UserCareerPreferenceRepository preferenceRepository;

    @InjectMocks
    private CareerPreferenceServiceImpl careerPreferenceService;

    @Test
    void hasCurrentPreferenceReturnsTrueWhenActivePreferenceExists() {
        when(preferenceRepository.existsByUserIdAndIsActiveTrue(USER_ID)).thenReturn(true);

        assertTrue(careerPreferenceService.hasCurrentPreference(USER_ID));
        verify(preferenceRepository).existsByUserIdAndIsActiveTrue(USER_ID);
    }

    @Test
    void hasCurrentPreferenceReturnsFalseWhenActivePreferenceDoesNotExist() {
        when(preferenceRepository.existsByUserIdAndIsActiveTrue(USER_ID)).thenReturn(false);

        assertFalse(careerPreferenceService.hasCurrentPreference(USER_ID));
        verify(preferenceRepository).existsByUserIdAndIsActiveTrue(USER_ID);
    }
}
