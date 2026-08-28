package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.model.UserCompetency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCompetencyRepository extends JpaRepository<UserCompetency, Long> {
    Optional<UserCompetency> findByUserIdAndCareerPreferenceId(Integer userId, Integer careerPreferenceId);

    Optional<UserCompetency> findFirstByUserIdOrderByUpdatedAtDesc(Integer userId);
}
