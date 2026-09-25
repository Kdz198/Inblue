package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.SessionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.request.JoinSessionDtoRequest;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.PaymentService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplActiveFlowTest {
    @Mock
    RestTemplate restTemplate;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    PaymentService paymentService;

    @Mock
    MentorReviewRepository mentorReviewRepository;

    @Mock
    MentorFeedbackRepository mentorFeedbackRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationDetailRepository applicationDetailRepository;

    private SessionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SessionServiceImpl(
                "https://daily.test",
                "key",
                "https://backend.test/webhook",
                sessionRepository,
                restTemplate,
                paymentService,
                mentorReviewRepository,
                mentorFeedbackRepository,
                applicationRepository,
                applicationDetailRepository);
    }

    @Test
    void candidateJoinMovesScheduledSessionToOngoingAndStoresParticipant() {
        Session session = new Session();
        session.setRoomName("room-1");
        session.setUserId(7);
        session.setUserId2(9);
        session.setStatus(SessionStatus.SCHEDULED);
        when(sessionRepository.findByRoomName("room-1")).thenReturn(session);

        service.saveJoinRecord(new JoinSessionDtoRequest("room-1", 7, "participant-1", false));

        assertEquals("participant-1", session.getParticipantId1());
        assertEquals(SessionStatus.ONGOING, session.getStatus());
        verify(sessionRepository).save(session);
    }

    @Test
    void candidateCannotJoinAnotherUsersSession() {
        Session session = new Session();
        session.setUserId(7);
        session.setUserId2(9);
        session.setStatus(SessionStatus.SCHEDULED);
        when(sessionRepository.findByRoomName("room-1")).thenReturn(session);

        CustomException error = assertThrows(
                CustomException.class,
                () -> service.saveJoinRecord(new JoinSessionDtoRequest("room-1", 99, "p", false)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void makePaymentRejectsSessionThatIsNotScheduled() {
        Session session = new Session();
        session.setId(3);
        session.setStatus(SessionStatus.ONGOING);
        when(sessionRepository.findById(3)).thenReturn(Optional.of(session));

        CustomException error = assertThrows(CustomException.class, () -> service.makePayment(3));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }
}
