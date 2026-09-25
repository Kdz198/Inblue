package fpt.org.inblue.service.submission.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.service.submission.SubmissionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EmailSubmissionServiceImplActiveFlowTest {
    @Mock
    EmailSubmissionRepository repository;

    @Mock
    CloudinaryService cloudinary;

    @Mock
    ObjectMapper mapper;

    @Mock
    SubmissionService submissionService;

    private EmailSubmissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailSubmissionServiceImpl(repository, cloudinary, mapper, submissionService);
    }

    @Test
    void getByIdReturnsRepositoryOptional() {
        EmailSubmission submission = new EmailSubmission();
        when(repository.findById(1L)).thenReturn(Optional.of(submission));
        assertEquals(Optional.of(submission), service.getById(1L));
    }

    @Test
    void getAllPreservesEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        assertEquals(List.of(), service.getAll());
    }

    @Test
    void fetchEmailsSkipsWhenImapConfigurationMissing() {
        ReflectionTestUtils.setField(service, "host", "");
        ReflectionTestUtils.setField(service, "username", "");
        service.fetchEmails();
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
