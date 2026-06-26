package fpt.org.inblue.service.submission;

import fpt.org.inblue.model.EmailSubmission;
import java.util.List;
import java.util.Optional;

public interface EmailSubmissionService {
    Optional<EmailSubmission> getById(Long id);

    List<EmailSubmission> getAll();
}
