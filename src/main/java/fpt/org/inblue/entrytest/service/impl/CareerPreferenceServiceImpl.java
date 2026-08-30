package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.service.CareerPreferenceService;
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

        boolean changedMainDirection = existingPreference.isEmpty()
                || !Objects.equals(preference.getTargetRole(), request.getTargetRole())
                || !Objects.equals(preference.getLanguagesJson(), request.getLanguagesJson());

        preference.setTargetRole(request.getTargetRole());
        preference.setLanguagesJson(request.getLanguagesJson());
        preference.setCareerGoal(request.getCareerGoal());
        preference.setTargetLevel(request.getTargetLevel());
        preference.setIsActive(true);
        if (changedMainDirection) {
            preference.setNeedRetest(true);
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
