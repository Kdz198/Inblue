package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.repository.CandidateProfileRepository;
import fpt.org.inblue.service.CandidateProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CandidateProfileImpl implements CandidateProfileService {
    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    public CandidateProfile createProfile(CandidateProfile profile) {
        System.out.println("candate service");
        return candidateProfileRepository.save(profile);
    }

    @Override
    public CandidateProfile getProfileByUserId(int userId) {
        CandidateProfile profile = candidateProfileRepository.findByUser_Id(userId);
        if (profile != null) {
            return profile;
        } else {
            return null;
        }
    }

    @Override
    public CandidateProfile getProfileByApplicationId(Long applicationId) {
        return candidateProfileRepository.findByApplicationId(applicationId);
    }

    @Override
    public CandidateProfile getProfileByUserIdAndApplicationIdIsNull(int userId) {
        return candidateProfileRepository.findByUser_IdAndApplicationIdIsNull(userId);
    }

    @Override
    public List<CandidateProfile> getAllProfiles() {
        return candidateProfileRepository.findAll();
    }

    @Override
    public void deleteProfile(int userId) {
        candidateProfileRepository.deleteByUser_Id(userId);
        candidateProfileRepository.flush();
    }

    @Override
    public CandidateProfile updateProfile(CandidateProfile profile) {
        return candidateProfileRepository.save(profile);
    }
}
