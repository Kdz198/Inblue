package fpt.org.inblue.repository;

import fpt.org.inblue.model.UserUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserUsageRepository extends JpaRepository<UserUsage, Integer> {
    Optional<UserUsage> findByUser_Id(int userId);
}

