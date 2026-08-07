package fpt.org.inblue.service;

import fpt.org.inblue.model.CandidateProfile;
import java.util.List;

public interface CandidateProfileService {
    CandidateProfile createProfile(CandidateProfile profile);

    List<CandidateProfile> getProfileByUserId(int userId);

    CandidateProfile getProfileByApplicationId(Long applicationId);

    CandidateProfile getProfileByUserIdAndApplicationIdIsNull(int userId);

    List<CandidateProfile> getAllProfiles();

    void deleteProfile(int userId);

    CandidateProfile updateProfile(CandidateProfile profile);
}
