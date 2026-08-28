package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.model.LevelScale;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.model.UserCompetency;
import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCompetencyServiceImpl implements fpt.org.inblue.entrytest.service.UserCompetencyService {
    private final UserCompetencyRepository competencyRepository;
    private final UserCareerPreferenceRepository preferenceRepository;
    private final LevelScaleRepository levelScaleRepository;

    @Override
    @Transactional
    public UserCompetency updateAfterEntryTest(EntryTestAttempt attempt) {
        UserCareerPreference preference = preferenceRepository
                .findById(attempt.getCareerPreferenceId())
                .orElseThrow(() -> new CustomException("Career preference not found", HttpStatus.NOT_FOUND));

        TargetLevel level = resolveLevel(
                preference.getTargetRole(),
                value(attempt.getFinalScore()),
                value(attempt.getSpecificCodingScore()));

        UserCompetency competency = competencyRepository
                .findByUserIdAndCareerPreferenceId(attempt.getUserId(), preference.getUserId())
                .orElseGet(() -> UserCompetency.builder()
                        .userId(attempt.getUserId())
                        .careerPreferenceId(preference.getUserId())
                        .build());

        competency.setTargetRole(preference.getTargetRole());
        competency.setLanguagesJson(preference.getLanguagesJson());
        competency.setCurrentLevel(level);
        competency.setCurrentScore(value(attempt.getFinalScore()));
        competency.setCommonQuizScore(value(attempt.getCommonQuizScore()));
        competency.setSpecificQuizScore(value(attempt.getSpecificQuizScore()));
        competency.setSpecificCodingScore(value(attempt.getSpecificCodingScore()));
        competency.setLastEntryTestAttemptId(attempt.getId());
        competency.setLastEvaluatedAt(LocalDateTime.now());
        competency.setCompetencySnapshotJson(buildSnapshot(attempt, level));

        preference.setNeedRetest(false);
        preferenceRepository.save(preference);
        return competencyRepository.save(competency);
    }

    @Override
    public String resolveLevelName(EntryTestAttempt attempt) {
        UserCareerPreference preference = preferenceRepository
                .findById(attempt.getCareerPreferenceId())
                .orElseThrow(() -> new CustomException("Career preference not found", HttpStatus.NOT_FOUND));
        return resolveLevel(
                        preference.getTargetRole(),
                        value(attempt.getFinalScore()),
                        value(attempt.getSpecificCodingScore()))
                .name();
    }

    @Override
    public UserCompetency getCurrentCompetency(Integer userId) {
        return competencyRepository
                .findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new CustomException("User competency not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public UserCompetency updateAfterJd(Integer userId, Double jdScore) {
        UserCompetency competency = getCurrentCompetency(userId);
        double oldScore = value(competency.getCurrentScore());
        double newScore = oldScore * 0.7 + value(jdScore) * 0.3;
        TargetLevel newLevel = resolveLevel(competency.getTargetRole(), newScore, value(competency.getSpecificCodingScore()));

        competency.setCurrentScore(round(newScore));
        competency.setCurrentLevel(newLevel);
        competency.setLastEvaluatedAt(LocalDateTime.now());
        return competencyRepository.save(competency);
    }

    private TargetLevel resolveLevel(TargetRole role, double finalScore, double codingScore) {
        return levelScaleRepository.findAllByIsActiveTrue().stream()
                .filter(scale -> scale.getTargetRole() == null || scale.getTargetRole() == role)
                .filter(scale -> finalScore >= value(scale.getMinScore()) && finalScore <= value(scale.getMaxScore()))
                .filter(scale -> scale.getMinCodingScore() == null || codingScore >= scale.getMinCodingScore())
                .max(Comparator.comparing(scale -> value(scale.getMinScore())))
                .map(LevelScale::getLevel)
                .orElseThrow(() -> new CustomException(
                        "Level scale is not configured for this score", HttpStatus.BAD_REQUEST));
    }

    private Map<String, Object> buildSnapshot(EntryTestAttempt attempt, TargetLevel level) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("level", level.name());
        snapshot.put("finalScore", attempt.getFinalScore());
        snapshot.put("commonQuizScore", attempt.getCommonQuizScore());
        snapshot.put("specificQuizScore", attempt.getSpecificQuizScore());
        snapshot.put("specificCodingScore", attempt.getSpecificCodingScore());
        return snapshot;
    }

    private double value(Double number) {
        return number == null ? 0.0 : number;
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
