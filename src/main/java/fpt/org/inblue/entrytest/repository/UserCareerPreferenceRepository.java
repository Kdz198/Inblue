package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.entity.UserCareerPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCareerPreferenceRepository extends JpaRepository<UserCareerPreference, Long> {
    Optional<UserCareerPreference> findByUserIdAndIsActiveTrue(Integer userId);

    boolean existsByUserIdAndIsActiveTrue(Integer userId);
}
