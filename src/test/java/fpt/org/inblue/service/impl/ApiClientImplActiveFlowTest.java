package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.service.LlmChatLogService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ApiClientImplActiveFlowTest {
    @Mock
    RestTemplate restTemplate;

    @Mock
    LlmChatLogService chatLogService;

    private ApiClientImpl service;

    @BeforeEach
    void setUp() {
        service = new ApiClientImpl(restTemplate, chatLogService);
        ReflectionTestUtils.setField(service, "LLM_BASE_URL", "http://llm");
    }

    @Test
    void callApiMapsJsonResponse() {
        when(restTemplate.exchange(
                        org.mockito.ArgumentMatchers.eq("http://llm/test"),
                        org.mockito.ArgumentMatchers.eq(HttpMethod.POST),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"value\":7}"));
        assertEquals(
                7,
                service.callApi(PythonService.LLM, "/test", HttpMethod.POST, Map.of(), Map.class)
                        .get("value"));
    }

    @Test
    void callApiSupportsBinaryResponse() {
        byte[] body = {1, 2};
        when(restTemplate.exchange(
                        org.mockito.ArgumentMatchers.eq("http://llm/audio"),
                        org.mockito.ArgumentMatchers.eq(HttpMethod.GET),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(body));
        assertArrayEquals(body, service.callApi(PythonService.LLM, "/audio", HttpMethod.GET, null, byte[].class));
    }

    @Test
    void callApiPreservesNullBody() {
        when(restTemplate.exchange(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(String.class)))
                .thenReturn(ResponseEntity.ok(null));
        assertNull(service.callApi(PythonService.LLM, "/empty", HttpMethod.GET, null, String.class));
    }

    @Test
    void callApiWrapsTransportFailure() {
        when(restTemplate.exchange(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(String.class)))
                .thenThrow(new RuntimeException("offline"));
        assertThrows(
                RuntimeException.class,
                () -> service.callApi(PythonService.LLM, "/test", HttpMethod.GET, null, String.class));
    }
}
