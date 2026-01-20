package fpt.org.inblue.repository;

import fpt.org.inblue.model.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Integer> {
    CandidateProfile findByUser_Id(int userId);

    void deleteByUser_Id(int userId);
}
