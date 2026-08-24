package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.entity.EntryTestAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryTestAttemptRepository extends JpaRepository<EntryTestAttempt, Long> {
    List<EntryTestAttempt> findAllByUserIdOrderByCreatedAtDesc(Integer userId);
}
