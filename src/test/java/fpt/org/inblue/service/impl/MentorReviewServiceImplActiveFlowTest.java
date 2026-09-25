package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.mapper.MentorReviewMapper;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MentorReviewServiceImplActiveFlowTest {
    @Mock
    MentorReviewRepository repository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    MentorReviewMapper mapper;

    @Mock
    MentorRepository mentorRepository;

    @Mock
    UserService userService;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    RoundRepository roundRepository;

    @Mock
    ApplicationService applicationService;

    @Mock
    MentorFeedbackRepository feedbackRepository;

    private MentorReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MentorReviewServiceImpl(
                repository,
                sessionRepository,
                mapper,
                mentorRepository,
                userService,
                detailRepository,
                roundRepository,
                applicationService,
                feedbackRepository);
    }

    @Test
    void getAllMentorReviewsReturnsRepositoryData() {
        MentorReview review = new MentorReview();
        when(repository.findAll()).thenReturn(List.of(review));
        assertEquals(List.of(review), service.getAllMentorReviews());
    }

    @Test
    void getMentorReviewByIdRejectsUnknownReview() {
        when(sessionRepository.existsById(9)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.getMentorReviewById(9));
    }
}
