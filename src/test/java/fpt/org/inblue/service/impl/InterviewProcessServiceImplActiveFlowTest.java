package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.KioskBookingRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.ProctoringService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewProcessServiceImplActiveFlowTest {
    @Mock
    InterviewSessionRedisRepository redisRepository;

    @Mock
    InterviewSessionRepository sessionRepository;

    @Mock
    ApiClient apiClient;

    @Mock
    ProctoringService proctoringService;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    KioskBookingRepository bookingRepository;

    private InterviewProcessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewProcessServiceImpl(
                redisRepository, sessionRepository, apiClient, proctoringService, detailRepository, bookingRepository);
    }

    @Test
    void currentQuestionRejectsExpiredSession() {
        when(redisRepository.findById("expired")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getCurrentQuestion("expired"));
    }

    @Test
    void timeoutWithoutSessionStillReturnsFinishedResponse() {
        when(redisRepository.findById("missing")).thenReturn(Optional.empty());
        var result = service.timeoutSession("missing");
        assertEquals(true, result.isFinished());
        assertEquals("missing", result.getSessionKey());
    }
}
