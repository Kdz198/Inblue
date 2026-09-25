package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.InterviewEnums;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewSessionServiceImplActiveFlowTest {
    @Mock
    ApiClient apiClient;

    @Mock
    InterviewSessionRepository repository;

    @Mock
    InterviewSessionRedisRepository redisRepository;

    private InterviewSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewSessionServiceImpl(apiClient, repository, redisRepository);
    }

    @Test
    void interviewConfigContainsAllFrontendOptionGroups() {
        var options = service.getInterviewConfigOptions();
        assertEquals(4, options.size());
        assertTrue(options.containsKey("interview_modes"));
        assertTrue(options.containsKey("difficulties"));
        assertTrue(options.containsKey("languages"));
        assertTrue(options.containsKey("domains"));
    }

    @Test
    void createSessionRejectsNullBlueprint() {
        InterviewSetupRequest request = setupRequest();
        when(apiClient.callApi(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(InterviewBlueprintResponse.class)))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.createSession(request));
    }

    @Test
    void createSessionRejectsEmptyBlueprint() {
        InterviewSetupRequest request = setupRequest();
        InterviewBlueprintResponse blueprint =
                InterviewBlueprintResponse.builder().blueprint(List.of()).build();
        when(apiClient.callApi(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(InterviewBlueprintResponse.class)))
                .thenReturn(blueprint);
        assertThrows(RuntimeException.class, () -> service.createSession(request));
    }

    @Test
    void createSessionPersistsDatabaseAndRedisState() {
        InterviewSetupRequest request = setupRequest();
        InterviewBlueprintResponse blueprint = InterviewBlueprintResponse.builder()
                .blueprint(List.of(new InterviewBlueprintResponse.InterviewPhase("Intro", 5, List.of())))
                .build();
        when(apiClient.callApi(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(InterviewBlueprintResponse.class)))
                .thenReturn(blueprint);
        when(repository.save(org.mockito.ArgumentMatchers.any(InterviewSession.class)))
                .thenAnswer(invocation -> {
                    InterviewSession session = invocation.getArgument(0);
                    session.setId(11);
                    return session;
                });

        String key = service.createSession(request);

        assertTrue(key != null && !key.isBlank());
        verify(redisRepository)
                .save(org.mockito.ArgumentMatchers.argThat(
                        value -> value.getDbId() == 11 && key.equals(value.getId())));
    }

    @Test
    void expiredInProgressSessionBecomesCancelledAndDropsBlueprint() {
        InterviewSession session = InterviewSession.builder()
                .id(1)
                .sessionKey("expired")
                .status(InterviewSession.SessionStatus.IN_PROGRESS)
                .blueprint(InterviewBlueprintResponse.builder()
                        .blueprint(new ArrayList<>())
                        .build())
                .build();
        when(repository.findByUserId(7)).thenReturn(List.of(session));
        when(redisRepository.findById("expired")).thenReturn(Optional.empty());
        when(repository.saveAll(List.of(session))).thenReturn(List.of(session));
        List<InterviewSession> result = service.getAllSessionsForUser(7);
        assertEquals(InterviewSession.SessionStatus.CANCELLED, result.getFirst().getStatus());
        assertNull(result.getFirst().getBlueprint());
        verify(repository).save(session);
    }

    @Test
    void liveInProgressSessionRemainsInProgress() {
        InterviewSession session = InterviewSession.builder()
                .id(1)
                .sessionKey("live")
                .status(InterviewSession.SessionStatus.IN_PROGRESS)
                .build();
        when(repository.findByUserId(7)).thenReturn(List.of(session));
        when(redisRepository.findById("live"))
                .thenReturn(
                        Optional.of(InterviewSessionRedis.builder().id("live").build()));
        when(repository.saveAll(List.of(session))).thenReturn(List.of(session));
        assertEquals(
                InterviewSession.SessionStatus.IN_PROGRESS,
                service.getAllSessionsForUser(7).getFirst().getStatus());
    }

    @Test
    void getSessionByIdRejectsUnknownSession() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getSessionById(99));
    }

    @Test
    void getSessionFromCacheReturnsExistingSession() {
        InterviewSessionRedis cached = InterviewSessionRedis.builder().id("key").build();
        when(redisRepository.findById("key")).thenReturn(Optional.of(cached));
        assertSame(cached, service.getSessionFromCache("key"));
    }

    @Test
    void getSessionFromCacheRejectsExpiredSession() {
        when(redisRepository.findById("expired")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getSessionFromCache("expired"));
    }

    private InterviewSetupRequest setupRequest() {
        return InterviewSetupRequest.builder()
                .userId(7)
                .applicationDetailId(5L)
                .sessionConfig(OrchestratorRequest.SessionConfigData.builder()
                        .interviewMode(InterviewEnums.InterviewMode.STANDARD_MOCK)
                        .domain(InterviewEnums.JobDomain.IT)
                        .build())
                .build();
    }
}
