package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.PaymentService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {
    private static final String DAILY_API_URL = "https://api.daily.co/v1";
    private static final String DAILY_WEBHOOKS_URL = DAILY_API_URL + "/webhooks";
    private static final String WEBHOOK_URL = "https://api.kdz.asia/api/sessions/webhooks/dailyco";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private MentorReviewRepository mentorReviewRepository;

    @Mock
    private MentorFeedbackRepository mentorFeedbackRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationDetailRepository applicationDetailRepository;

    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionServiceImpl(
                DAILY_API_URL,
                "daily-api-key",
                WEBHOOK_URL,
                sessionRepository,
                restTemplate,
                paymentService,
                mentorReviewRepository,
                mentorFeedbackRepository,
                applicationRepository,
                applicationDetailRepository);
    }

    @Test
    void reactivateWebhookUpdatesExistingWebhook() {
        List<Map<String, Object>> existingWebhooks = List.of(Map.of("uuid", "webhook-uuid", "url", WEBHOOK_URL));

        when(restTemplate.exchange(
                        eq(DAILY_WEBHOOKS_URL),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(existingWebhooks, HttpStatus.OK));
        when(restTemplate.exchange(
                        eq(DAILY_WEBHOOKS_URL + "/webhook-uuid"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(ResponseEntity.ok("updated"));

        String result = sessionService.reactivateWebhook();

        assertEquals("updated", result);
        verify(restTemplate, never())
                .exchange(eq(DAILY_WEBHOOKS_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void reactivateWebhookCreatesWebhookWhenNoneExists() {
        when(restTemplate.exchange(
                        eq(DAILY_WEBHOOKS_URL),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));
        when(restTemplate.exchange(
                        eq(DAILY_WEBHOOKS_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("created"));

        String result = sessionService.reactivateWebhook();

        assertEquals("created", result);
    }
}
