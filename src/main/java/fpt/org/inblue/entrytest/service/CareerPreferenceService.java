package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.model.UserCareerPreference;

public interface CareerPreferenceService {
    UserCareerPreference getCurrentPreference(Integer userId);

    boolean hasCurrentPreference(Integer userId);

    UserCareerPreference upsertPreference(Integer userId, UpsertCareerPreferenceRequest request);

    UserCareerPreference skipPreference(Integer userId);
}
