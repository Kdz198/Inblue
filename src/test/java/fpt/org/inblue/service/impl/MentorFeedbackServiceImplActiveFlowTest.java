package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import fpt.org.inblue.mapper.MentorFeedbackMapper;
import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.MentorReviewService;
import fpt.org.inblue.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MentorFeedbackServiceImplActiveFlowTest {
    @Mock
    MentorFeedbackRepository repository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    MentorFeedbackMapper mapper;

    @Mock
    MentorRepository mentorRepository;

    @Mock
    UserService userService;

    @Mock
    MentorReviewService mentorReviewService;

    private MentorFeedbackServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MentorFeedbackServiceImpl(
                repository, sessionRepository, mapper, mentorRepository, userService, mentorReviewService);
    }

    @Test
    void getAllMentorFeedbacksReturnsRepositoryData() {
        MentorFeedback feedback = new MentorFeedback();
        feedback.setId(1);
        when(repository.findAll()).thenReturn(List.of(feedback));
        assertEquals(List.of(feedback), service.getAllMentorFeedbacks());
    }

    @Test
    void getAllByMentorDelegatesMentorFilter() {
        when(repository.findAllByMentor_Id(3)).thenReturn(List.of());
        assertEquals(List.of(), service.getAllByMentor(3));
    }
}
