package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.service.submission.EmailSubmissionService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSubmissionServiceImpl implements EmailSubmissionService {

    private final EmailSubmissionRepository emailSubmissionRepository;

    @Override
    public Optional<EmailSubmission> getById(Long id) {
        return emailSubmissionRepository.findById(id);
    }

    @Override
    public List<EmailSubmission> getAll() {
        return emailSubmissionRepository.findAll();
    }
}
