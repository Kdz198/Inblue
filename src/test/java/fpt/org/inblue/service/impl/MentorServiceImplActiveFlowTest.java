package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.EmbeddingService;
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
    UserRepository userRepository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    ApplicationDetailRepository applicationDetailRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    MentorReviewRepository reviewRepository;

    @Mock
    MentorFeedbackRepository feedbackRepository;

    @Mock
    JobDescriptionRepository jobDescriptionRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    CloudinaryService cloudinary;

    @Mock
    MentorMapper mapper;

    @Mock
    PasswordEncoder encoder;

    @Mock
    EmbeddingService embeddingService;

    private MentorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MentorServiceImpl(
                repository,
                userRepository,
                sessionRepository,
                applicationDetailRepository,
                applicationRepository,
                reviewRepository,
                feedbackRepository,
                jobDescriptionRepository,
                publisher,
                cloudinary,
                mapper,
                encoder,
                embeddingService);
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

    @Test
    void getAllMentorsMapsRepositoryEntities() {
        Mentor mentor = Mentor.builder().id(3).build();
        MentorResponse response = new MentorResponse();
        when(repository.findAll()).thenReturn(java.util.List.of(mentor));
        when(mapper.toMentorResponse(mentor)).thenReturn(response);
        assertEquals(java.util.List.of(response), service.getAllMentors());
    }

    @Test
    void recommendedMentorsReturnEmptyWhenJobHasNoEmbedding() {
        JobDescription jd = JobDescription.builder().id(8L).build();
        when(jobDescriptionRepository.findById(8L)).thenReturn(Optional.of(jd));
        assertEquals(java.util.List.of(), service.getTopRecommendedMentors(8L));
    }

    @Test
    void recommendedMentorsRejectUnknownJob() {
        when(jobDescriptionRepository.findById(8L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getTopRecommendedMentors(8L))
                        .getStatus()
                        .value());
    }
}
