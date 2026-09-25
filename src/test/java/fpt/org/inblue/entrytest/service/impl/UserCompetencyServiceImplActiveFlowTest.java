package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.model.LevelScale;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.model.UserCompetency;
import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCompetencyServiceImplActiveFlowTest {
    @Mock
    UserCompetencyRepository competencyRepository;

    @Mock
    UserCareerPreferenceRepository preferenceRepository;

    @Mock
    LevelScaleRepository levelScaleRepository;

    private UserCompetencyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserCompetencyServiceImpl(competencyRepository, preferenceRepository, levelScaleRepository);
    }

    @Test
    void updateAfterEntryTestRejectsMissingPreference() {
        EntryTestAttempt attempt =
                EntryTestAttempt.builder().careerPreferenceId(7).build();
        when(preferenceRepository.findById(7)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.updateAfterEntryTest(attempt));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void resolveLevelUsesMostSpecificMatchingScale() {
        EntryTestAttempt attempt = attempt(80.0, 30.0);
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(preference()));
        when(levelScaleRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(
                        scale(null, TargetLevel.INTERN, 0, 100, null),
                        scale(TargetRole.BE, TargetLevel.JUNIOR, 70, 89.99, 20.0)));

        assertEquals("JUNIOR", service.resolveLevelName(attempt));
    }

    @Test
    void resolveLevelRejectsMissingScaleCoverage() {
        EntryTestAttempt attempt = attempt(101.0, 50.0);
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(preference()));
        when(levelScaleRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(scale(TargetRole.BE, TargetLevel.MIDDLE, 80, 100, 30.0)));

        CustomException error = assertThrows(CustomException.class, () -> service.resolveLevelName(attempt));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void resolveLevelHonorsMinimumCodingScoreBoundary() {
        EntryTestAttempt attempt = attempt(80.0, 19.99);
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(preference()));
        when(levelScaleRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(scale(TargetRole.BE, TargetLevel.JUNIOR, 70, 89.99, 20.0)));

        assertThrows(CustomException.class, () -> service.resolveLevelName(attempt));
    }

    @Test
    void updateAfterEntryTestCreatesSnapshotAndClearsRetestFlag() {
        EntryTestAttempt attempt = attempt(80.0, 30.0);
        UserCareerPreference preference = preference();
        when(preferenceRepository.findById(7)).thenReturn(Optional.of(preference));
        when(levelScaleRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(scale(TargetRole.BE, TargetLevel.JUNIOR, 70, 89.99, 20.0)));
        when(competencyRepository.findByUser_IdAndCareerPreferenceId(5, 7)).thenReturn(Optional.empty());
        when(competencyRepository.save(org.mockito.ArgumentMatchers.any(UserCompetency.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCompetency result = service.updateAfterEntryTest(attempt);

        assertSame(attempt.getUser(), result.getUser());
        assertEquals(TargetLevel.JUNIOR, result.getCurrentLevel());
        assertEquals("JUNIOR", result.getCompetencySnapshotJson().get("level"));
        assertFalse(preference.getNeedRetest());
        verify(preferenceRepository).save(preference);
    }

    @Test
    void getCurrentCompetencyRejectsMissingUserResult() {
        when(competencyRepository.findFirstByUser_IdOrderByUpdatedAtDesc(5)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.getCurrentCompetency(5));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void updateAfterJdUsesWeightedScoreAndRoundsToTwoDecimals() {
        UserCompetency existing = UserCompetency.builder()
                .user(User.builder().id(5).build())
                .targetRole(TargetRole.BE)
                .currentScore(81.11)
                .specificCodingScore(30.0)
                .build();
        when(competencyRepository.findFirstByUser_IdOrderByUpdatedAtDesc(5)).thenReturn(Optional.of(existing));
        when(levelScaleRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(scale(TargetRole.BE, TargetLevel.JUNIOR, 0, 100, 20.0)));
        when(competencyRepository.save(existing)).thenReturn(existing);

        UserCompetency result = service.updateAfterJd(5, 70.0);

        assertEquals(77.78, result.getCurrentScore());
        assertEquals(TargetLevel.JUNIOR, result.getCurrentLevel());
    }

    private EntryTestAttempt attempt(double finalScore, double codingScore) {
        return EntryTestAttempt.builder()
                .id(9L)
                .user(User.builder().id(5).build())
                .careerPreferenceId(7)
                .finalScore(finalScore)
                .specificCodingScore(codingScore)
                .build();
    }

    private UserCareerPreference preference() {
        return UserCareerPreference.builder()
                .userId(7)
                .targetRole(TargetRole.BE)
                .skills(List.of("Java"))
                .needRetest(true)
                .build();
    }

    private LevelScale scale(TargetRole role, TargetLevel level, double min, double max, Double coding) {
        return LevelScale.builder()
                .targetRole(role)
                .level(level)
                .minScore(min)
                .maxScore(max)
                .minCodingScore(coding)
                .isActive(true)
                .build();
    }
}
