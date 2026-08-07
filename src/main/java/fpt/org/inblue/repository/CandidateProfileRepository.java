package fpt.org.inblue.repository;

import fpt.org.inblue.model.CandidateProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Integer> {
    List<CandidateProfile> findByUser_Id(int userId);

    CandidateProfile findByApplicationId(Long applicationId);

    CandidateProfile findByUser_IdAndApplicationIdIsNull(int userId);

    void deleteByUser_Id(int userId);
}
