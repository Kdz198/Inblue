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
    public List<CandidateProfile> getProfileByUserId(int userId) {
        return candidateProfileRepository.findByUser_Id(userId);
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
        if (profile.getId() != null) {
            candidateProfileRepository.findById(profile.getId()).ifPresent(existing -> {
                if (profile.getUser() == null) {
                    profile.setUser(existing.getUser());
                }
                if (profile.getApplicationId() == null) {
                    profile.setApplicationId(existing.getApplicationId());
                }
                if (profile.getCreatedAt() == null) {
                    profile.setCreatedAt(existing.getCreatedAt());
                }
            });
        }
        return candidateProfileRepository.save(profile);
    }
}
