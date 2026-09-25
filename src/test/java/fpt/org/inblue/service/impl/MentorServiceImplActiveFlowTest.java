package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplActiveFlowTest {
    @Mock
    MentorRepository repository;

    @Mock
    MentorFeedbackRepository feedbackRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    CloudinaryService cloudinary;

    @Mock
    MentorMapper mapper;

    @Mock
    PasswordEncoder encoder;

    private MentorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MentorServiceImpl(repository, feedbackRepository, publisher, cloudinary, mapper, encoder);
    }

    @Test
    void getMentorByIdRejectsUnknownMentor() {
        when(repository.existsById(9)).thenReturn(false);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getMentorById(9))
                        .getStatus()
                        .value());
    }

    @Test
    void toggleActiveSwitchesExistingMentor() {
        Mentor mentor = Mentor.builder().id(3).isActive(true).build();
        when(repository.existsById(3)).thenReturn(true);
        when(repository.findById(3)).thenReturn(Optional.of(mentor));
        service.toggleActive(3);
        assertEquals(false, mentor.isActive());
        verify(repository).save(mentor);
    }
}
