package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.entity.EntryTest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryTestRepository extends JpaRepository<EntryTest, Long> {
    Optional<EntryTest> findFirstByIsActiveTrueOrderByVersionDesc();
}
