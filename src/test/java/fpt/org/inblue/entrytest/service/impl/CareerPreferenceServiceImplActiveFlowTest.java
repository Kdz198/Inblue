package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.service.EmbeddingService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CareerPreferenceServiceImplActiveFlowTest {
    @Mock
    UserCareerPreferenceRepository preferenceRepository;

    @Mock
    EmbeddingService embeddingService;

    private CareerPreferenceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CareerPreferenceServiceImpl(preferenceRepository, embeddingService);
    }

    @Test
    void getCurrentPreferenceReturnsActivePreference() {
        UserCareerPreference preference =
                UserCareerPreference.builder().userId(7).build();
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference));

        assertSame(preference, service.getCurrentPreference(7));
    }

    @Test
    void getCurrentPreferenceRejectsMissingPreference() {
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.empty());

        CustomException error = assertThrows(CustomException.class, () -> service.getCurrentPreference(7));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void hasCurrentPreferenceDelegatesToActiveLookup() {
        when(preferenceRepository.existsByUserIdAndIsActiveTrue(7)).thenReturn(true);

        assertTrue(service.hasCurrentPreference(7));
    }

    @Test
    void upsertRejectsMissingTargetRoleBeforeWriting() {
        UpsertCareerPreferenceRequest request =
                UpsertCareerPreferenceRequest.builder().build();

        CustomException error = assertThrows(CustomException.class, () -> service.upsertPreference(7, request));
        assertEquals(400, error.getStatus().value());
        verify(preferenceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void upsertNewPreferenceCleansLanguagesAndGeneratesEmbedding() {
        UpsertCareerPreferenceRequest request = UpsertCareerPreferenceRequest.builder()
                .targetRole(TargetRole.BE)
                .targetLevel(TargetLevel.JUNIOR)
                .languagesJson(List.of(" Java ", "", "Spring"))
                .careerGoal("Backend engineer")
                .build();
        float[] vector = {0.2f, 0.8f};
        when(preferenceRepository.findById(7)).thenReturn(Optional.empty());
        when(embeddingService.generateEmbedding("Java, Spring")).thenReturn(vector);
        when(preferenceRepository.save(org.mockito.ArgumentMatchers.any(UserCareerPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCareerPreference saved = service.upsertPreference(7, request);

        assertEquals(7, saved.getUserId());
        assertEquals(TargetRole.BE, saved.getTargetRole());
        assertEquals(TargetLevel.JUNIOR, saved.getTargetLevel());
        assertTrue(saved.getNeedRetest());
        assertArrayEquals(vector, saved.getSkillEmbedding());
    }

    @Test
    void upsertUnchangedDirectionPreservesRetestFlagAndEmbedding() {
        float[] vector = {1.0f};
        UserCareerPreference existing = UserCareerPreference.builder()
                .userId(7)
                .targetRole(TargetRole.FE)
                .skills(List.of("TypeScript"))
                .skillEmbedding(vector)
                .needRetest(false)
                .isActive(false)
                .build();
        UpsertCareerPreferenceRequest request = UpsertCareerPreferenceRequest.builder()
                .targetRole(TargetRole.FE)
                .languagesJson(List.of("TypeScript"))
                .build();
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(existing));
        when(preferenceRepository.save(existing)).thenReturn(existing);

        UserCareerPreference saved = service.upsertPreference(7, request);

        assertFalse(saved.getNeedRetest());
        assertTrue(saved.getIsActive());
        assertSame(vector, saved.getSkillEmbedding());
        verify(embeddingService, never()).generateEmbedding(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void upsertBlankLanguagesClearsEmbedding() {
        UserCareerPreference existing = UserCareerPreference.builder()
                .userId(7)
                .targetRole(TargetRole.BE)
                .skills(List.of("Java"))
                .skillEmbedding(new float[] {1.0f})
                .needRetest(false)
                .build();
        UpsertCareerPreferenceRequest request = UpsertCareerPreferenceRequest.builder()
                .targetRole(TargetRole.BE)
                .languagesJson(List.of(" ", ""))
                .build();
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(existing));
        when(preferenceRepository.save(existing)).thenReturn(existing);

        UserCareerPreference saved = service.upsertPreference(7, request);

        assertNull(saved.getSkillEmbedding());
        assertTrue(saved.getNeedRetest());
    }

    @Test
    void skipPreferenceCreatesActiveRetestMarkerWhenMissing() {
        when(preferenceRepository.findById(7)).thenReturn(Optional.empty());
        when(preferenceRepository.save(org.mockito.ArgumentMatchers.any(UserCareerPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCareerPreference saved = service.skipPreference(7);

        assertEquals(7, saved.getUserId());
        assertTrue(saved.getIsActive());
        assertTrue(saved.getNeedRetest());
    }
}
