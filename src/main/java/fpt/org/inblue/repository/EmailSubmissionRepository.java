package fpt.org.inblue.repository;

import fpt.org.inblue.model.EmailSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailSubmissionRepository extends JpaRepository<EmailSubmission, Long> {
    List<EmailSubmission> findByStatus(EmailSubmission.EmailStatus status);
}
