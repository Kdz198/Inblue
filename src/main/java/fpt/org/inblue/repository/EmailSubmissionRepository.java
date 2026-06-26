package fpt.org.inblue.repository;

import fpt.org.inblue.model.EmailSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSubmissionRepository extends JpaRepository<EmailSubmission, Long> {
    List<EmailSubmission> findByStatus(EmailSubmission.EmailStatus status);
    List<EmailSubmission> findByStatusAndApplicationIdOrderByIdDesc(EmailSubmission.EmailStatus status, Long applicationId);
}
