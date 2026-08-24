package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.entity.UserCompetency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCompetencyRepository extends JpaRepository<UserCompetency, Long> {
    Optional<UserCompetency> findByUserIdAndCareerPreferenceId(Integer userId, Long careerPreferenceId);

    Optional<UserCompetency> findFirstByUserIdOrderByUpdatedAtDesc(Integer userId);
}
