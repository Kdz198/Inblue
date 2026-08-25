package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.entity.UserCareerPreference;
import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;

public interface CareerPreferenceService {
    UserCareerPreference getCurrentPreference(Integer userId);

    boolean hasCurrentPreference(Integer userId);

    UserCareerPreference upsertPreference(Integer userId, UpsertCareerPreferenceRequest request);

    UserCareerPreference skipPreference(Integer userId);
}
