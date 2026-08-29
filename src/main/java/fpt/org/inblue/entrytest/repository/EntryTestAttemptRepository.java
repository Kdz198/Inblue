package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryTestAttemptRepository extends JpaRepository<EntryTestAttempt, Long> {
    List<EntryTestAttempt> findAllByUser_IdOrderByCreatedAtDesc(Integer userId);

    Optional<EntryTestAttempt> findByIdAndUser_Id(Long attemptId, Integer userId);
}
