package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import fpt.org.inblue.service.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SpeechServiceImplActiveFlowTest {
    @Mock
    RestTemplate restTemplate;

    @Mock
    ApiClient apiClient;

    @Mock
    StringRedisTemplate redisTemplate;

    private SpeechServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SpeechServiceImpl(restTemplate, apiClient, redisTemplate);
    }

    @Test
    void enhancedTranscriptUsesDedicatedWorkspace() {
        EnhanceTranscriptRequest request = new EnhanceTranscriptRequest();
        when(apiClient.sendChatToAnythingLlm(
                        AnythingLlmWorkspace.ENHANCE_TRANSCRIPT, request, "transcript", true, null, String.class))
                .thenReturn("clean");
        assertEquals("clean", service.enhancedTranscript(request));
    }

    @Test
    void generateAudioFromPythonDelegatesPayload() {
        byte[] audio = {1, 2, 3};
        when(apiClient.callApi(
                        org.mockito.ArgumentMatchers.eq(PythonService.LLM),
                        org.mockito.ArgumentMatchers.eq("/api/v1/tts"),
                        org.mockito.ArgumentMatchers.eq(HttpMethod.POST),
                        org.mockito.ArgumentMatchers.anyMap(),
                        org.mockito.ArgumentMatchers.eq(byte[].class)))
                .thenReturn(audio);
        assertSame(audio, service.generateAudioFromPython("Xin chao", "vi"));
        verify(apiClient)
                .callApi(
                        org.mockito.ArgumentMatchers.eq(PythonService.LLM),
                        org.mockito.ArgumentMatchers.eq("/api/v1/tts"),
                        org.mockito.ArgumentMatchers.eq(HttpMethod.POST),
                        org.mockito.ArgumentMatchers.argThat(body -> {
                            if (!(body instanceof java.util.Map<?, ?> map)) return false;
                            return "Xin chao".equals(map.get("text")) && "vi".equals(map.get("voice"));
                        }),
                        org.mockito.ArgumentMatchers.eq(byte[].class));
    }
}
