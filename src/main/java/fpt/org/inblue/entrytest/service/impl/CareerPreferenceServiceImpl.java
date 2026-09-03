package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.service.CareerPreferenceService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.service.EmbeddingService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CareerPreferenceServiceImpl implements CareerPreferenceService {
    private final UserCareerPreferenceRepository preferenceRepository;
    private final EmbeddingService embeddingService;

    @Override
    public UserCareerPreference getCurrentPreference(Integer userId) {
        return preferenceRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new CustomException("Career preference not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCurrentPreference(Integer userId) {
        return preferenceRepository.existsByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    public UserCareerPreference upsertPreference(Integer userId, UpsertCareerPreferenceRequest request) {
        if (request.getTargetRole() == null) {
            throw new CustomException("targetRole is required", HttpStatus.BAD_REQUEST);
        }

        Optional<UserCareerPreference> existingPreference = preferenceRepository.findById(userId);
        UserCareerPreference preference = existingPreference.orElseGet(() -> UserCareerPreference.builder()
                .userId(userId)
                .isActive(true)
                .needRetest(true)
                .build());

        boolean changedSkills =
                existingPreference.isEmpty() || !Objects.equals(preference.getSkills(), request.getLanguagesJson());

        boolean changedMainDirection = existingPreference.isEmpty()
                || !Objects.equals(preference.getTargetRole(), request.getTargetRole())
                || changedSkills;

        preference.setTargetRole(request.getTargetRole());
        preference.setSkills(request.getLanguagesJson());
        preference.setCareerGoal(request.getCareerGoal());
        preference.setTargetLevel(request.getTargetLevel());
        preference.setIsActive(true);
        if (changedMainDirection) {
            preference.setNeedRetest(true);
        }

        if (changedSkills || preference.getSkillEmbedding() == null) {
            List<String> languages = request.getLanguagesJson();
            if (languages != null && !languages.isEmpty()) {
                List<String> cleanLanguages = languages.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(String::trim)
                        .toList();
                if (!cleanLanguages.isEmpty()) {
                    preference.setSkillEmbedding(embeddingService.generateEmbedding(String.join(", ", cleanLanguages)));
                } else {
                    preference.setSkillEmbedding(null);
                }
            } else {
                preference.setSkillEmbedding(null);
            }
        }

        return preferenceRepository.save(preference);
    }

    @Override
    @Transactional
    public UserCareerPreference skipPreference(Integer userId) {
        UserCareerPreference preference = preferenceRepository
                .findById(userId)
                .orElseGet(() -> UserCareerPreference.builder()
                        .userId(userId)
                        .isActive(true)
                        .needRetest(true)
                        .build());
        preference.setIsActive(true);
        preference.setNeedRetest(true);
        return preferenceRepository.save(preference);
    }
}
