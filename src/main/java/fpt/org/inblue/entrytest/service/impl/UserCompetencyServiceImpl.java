package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.model.LevelScale;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.model.UserCompetency;
import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
import fpt.org.inblue.entrytest.service.UserCompetencyService;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.User;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCompetencyServiceImpl implements UserCompetencyService {
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
                preference.getTargetRole(), value(attempt.getFinalScore()), value(attempt.getSpecificCodingScore()));
        User user = attempt.getUser();

        UserCompetency competency = competencyRepository
                .findByUser_IdAndCareerPreferenceId(user.getId(), preference.getUserId())
                .orElseGet(() -> UserCompetency.builder()
                        .user(user)
                        .careerPreferenceId(preference.getUserId())
                        .build());

        competency.setTargetRole(preference.getTargetRole());
        competency.setLanguagesJson(preference.getSkills());
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
                .findFirstByUser_IdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new CustomException("User competency not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public UserCompetency updateAfterJd(Integer userId, Double jdScore) {
        UserCompetency competency = getCurrentCompetency(userId);
        double oldScore = value(competency.getCurrentScore());
        double newScore = round(oldScore * 0.7 + value(jdScore) * 0.3);
        TargetLevel newLevel =
                resolveLevel(competency.getTargetRole(), newScore, value(competency.getSpecificCodingScore()));

        competency.setCurrentScore(round(newScore));
        competency.setCurrentLevel(newLevel);
        competency.setLastEvaluatedAt(LocalDateTime.now());
        return competencyRepository.save(competency);
    }

    private TargetLevel resolveLevel(TargetRole role, double finalScore, double codingScore) {
        // Mỗi level lấy 1 scale: scale riêng của role ưu tiên hơn scale chung (targetRole = null).
        // TreeMap theo thứ tự enum => thang luôn đi từ INTERN -> MIDDLE, không phụ thuộc admin nhập minScore
        Map<TargetLevel, LevelScale> scaleByLevel = new TreeMap<>();
        levelScaleRepository.findAllByIsActiveTrue().stream()
                .filter(scale -> scale.getLevel() != null)
                .filter(scale -> scale.getTargetRole() == null || scale.getTargetRole() == role)
                .forEach(scale -> scaleByLevel.merge(
                        scale.getLevel(), scale, (current, next) -> next.getTargetRole() != null ? next : current));
        List<LevelScale> scales = List.copyOf(scaleByLevel.values());
        if (scales.isEmpty()) {
            throw new CustomException("Level scale is not configured for this role", HttpStatus.BAD_REQUEST);
        }

        // Level cao nhất đạt theo final score và theo coding score (không đạt mức nào -> level thấp nhất)
        int scoreIndex = highestReachedIndex(scales, scale -> finalScore >= value(scale.getMinScore()));
        int codingIndex = highestReachedIndex(scales, scale -> codingScore >= value(scale.getMinCodingScore()));

        // Lấy level thấp hơn trong 2 level: điểm thường đủ mà coding thiếu -> theo coding, và ngược lại
        LevelScale resolved = scales.get(Math.min(scoreIndex, codingIndex));
        log.info(
                "Resolve level: role={}, finalScore={}, codingScore={}, scoreLevel={}, codingLevel={}, resolvedLevel={}",
                role,
                finalScore,
                codingScore,
                scales.get(scoreIndex).getLevel(),
                scales.get(codingIndex).getLevel(),
                resolved.getLevel());
        return resolved.getLevel();
    }

    // Leo thang từ level thấp nhất, dừng ở bậc đầu tiên không đạt (level thấp nhất luôn là mức sàn)
    private int highestReachedIndex(List<LevelScale> scales, Predicate<LevelScale> reached) {
        int index = 0;
        for (int i = 1; i < scales.size() && reached.test(scales.get(i)); i++) {
            index = i;
        }
        return index;
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
