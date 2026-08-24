package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.entrytest.entity.UserCareerPreference;
import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.service.CareerPreferenceService;
import java.util.Objects;
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
    @Transactional
    public UserCareerPreference upsertPreference(Integer userId, UpsertCareerPreferenceRequest request) {
        if (request.getTargetRole() == null) {
            throw new CustomException("targetRole is required", HttpStatus.BAD_REQUEST);
        }

        UserCareerPreference preference = preferenceRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseGet(() -> UserCareerPreference.builder()
                        .userId(userId)
                        .isActive(true)
                        .needRetest(true)
                        .build());

        boolean changedMainDirection = preference.getId() == null
                || !Objects.equals(preference.getTargetRole(), request.getTargetRole())
                || !Objects.equals(preference.getLanguagesJson(), request.getLanguagesJson());

        preference.setTargetRole(request.getTargetRole());
        preference.setLanguagesJson(request.getLanguagesJson());
        preference.setCareerGoal(request.getCareerGoal());
        preference.setTargetLevel(request.getTargetLevel());
        if (changedMainDirection) {
            preference.setNeedRetest(true);
        }

        return preferenceRepository.save(preference);
    }

    @Override
    @Transactional
    public UserCareerPreference skipPreference(Integer userId) {
        UserCareerPreference preference = preferenceRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseGet(() -> UserCareerPreference.builder()
                        .userId(userId)
                        .isActive(true)
                        .needRetest(true)
                        .build());
        preference.setNeedRetest(true);
        return preferenceRepository.save(preference);
    }
}
