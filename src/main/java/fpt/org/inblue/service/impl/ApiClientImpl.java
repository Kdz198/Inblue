package fpt.org.inblue.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.LlmChatLogService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiClientImpl implements ApiClient {

    private final RestTemplate restTemplate;

    @Value("${PYTHON_LLM_URL:}")
    private String LLM_BASE_URL;

    @Value("${PYTHON_VISION_URL:}")
    private String VISION_BASE_URL;

    // --- CẤU HÌNH ANYTHING LLM MỚI ---
    @Value("${ANYTHING_LLM_URL:}")
    private String ANYTHING_LLM_URL;

    @Value("${ANYTHING_LLM_API_KEY:}")
    private String ANYTHING_LLM_API_KEY;

    // --- CẤU HÌNH COMPILER SERVICE MỚI ---
    @Value("${COMPILER_SERVICE_URL:}")
    private String COMPILER_SERVICE_URL;

    private final LlmChatLogService chatLogService;

    private String getBaseUrl(PythonService targetService) {
        return targetService == PythonService.VISION ? VISION_BASE_URL : LLM_BASE_URL;
    }

    @Override
    public <T> T sendChatToAnythingLlm(
            AnythingLlmWorkspace workspace,
            Object payload,
            String sessionId,
            boolean reset,
            List<MultipartFile> files,
            Class<T> responseType) {

        String endpoint = "/workspace/" + workspace.getSlug() + "/chat";

        // 1. XỬ LÝ PAYLOAD (Ép kiểu thông minh)
        String message;
        try {
            if (payload instanceof String) {
                message = (String) payload;
            } else {
                ObjectMapper payloadMapper = new ObjectMapper();
                // Tắt pretty print để ép JSON thành 1 dòng, tiết kiệm Token
                payloadMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
                message = payloadMapper.writeValueAsString(payload);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse payload thành JSON String: " + e.getMessage(), e);
        }

        // 2. BUILD BODY REQUEST
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("mode", "chat");
        requestBody.put("sessionId", sessionId);
        requestBody.put("reset", reset);

        // 3. XỬ LÝ ĐÍNH KÈM FILE (DIRECT CONTEXT INJECTION)
        if (files != null && !files.isEmpty()) {
            List<Map<String, String>> attachments = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    String originalName = file.getOriginalFilename();
                    String realMimeType = file.getContentType();
                    if (realMimeType == null) {
                        realMimeType = "application/octet-stream";
                    }

                    // Luật MIME type của AnythingLLM
                    String payloadMimeType =
                            realMimeType.startsWith("image/") ? realMimeType : "application/anythingllm-document";

                    // Chuyển file sang Base64
                    byte[] fileBytes = file.getBytes();
                    String base64String = java.util.Base64.getEncoder().encodeToString(fileBytes);
                    String contentString = "data:" + realMimeType + ";base64," + base64String;

                    Map<String, String> attachment = new HashMap<>();
                    attachment.put("name", originalName);
                    attachment.put("mime", payloadMimeType);
                    attachment.put("contentString", contentString);

                    attachments.add(attachment);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Lỗi encode file [" + file.getOriginalFilename() + "]: " + e.getMessage(), e);
                }
            }
            requestBody.put("attachments", attachments);
        }

        // 4. CONFIG HEADERS & HTTP ENTITY
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ANYTHING_LLM_API_KEY);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 5. GỌI API & BÓC TÁCH RESPONSE
        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response =
                    restTemplate.exchange(ANYTHING_LLM_URL + endpoint, HttpMethod.POST, requestEntity, String.class);
            long responseTimeMs = System.currentTimeMillis() - startTime;
            if (response.getBody() == null) return null;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String textResponse = rootNode.path("textResponse").asText();

            if (textResponse == null || textResponse.trim().isEmpty()) {
                return null;
            }

            // ==========================================================
            // 🛠 FIX LỖI MARKDOWN TRƯỚC KHI PARSE (JSON SANITIZATION)
            // ==========================================================
            String cleanJson = textResponse.trim();
            // Xóa thẻ ```json hoặc ```JSON hoặc ``` ở đầu chuỗi
            cleanJson = cleanJson.replaceAll("^```(?i)json\\s*", "").replaceAll("^```\\s*", "");
            // Xóa thẻ ``` ở cuối chuỗi
            cleanJson = cleanJson.replaceAll("\\s*```$", "");
            // ==========================================================

            // Lấy thông tin Metrics (Tokens)
            JsonNode metricsNode = rootNode.path("metrics");
            Integer promptTokens = metricsNode.path("prompt_tokens").asInt(0);
            Integer completionTokens = metricsNode.path("completion_tokens").asInt(0);

            // Lấy TraceID từ Context của Filter
            String traceId = org.slf4j.MDC.get("traceId");
            if (traceId == null) {
                traceId = "no-trace-id";
            }

            chatLogService.saveLog(
                    traceId,
                    sessionId,
                    workspace.name(),
                    message,
                    cleanJson,
                    promptTokens,
                    completionTokens,
                    responseTimeMs);

            // [CHỐT CHẶN AN TOÀN] Nếu backend chỉ cần trả về String thuần
            if (responseType.equals(String.class)) {
                return (T) cleanJson;
            }

            // Nếu backend cần Object, map JSON từ chuỗi đã dọn dẹp về Object đó
            return objectMapper.readValue(cleanJson, responseType);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Chat AnythingLLM [" + endpoint + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T callApi(
            PythonService targetService,
            String endpoint,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> requestEntity;

        String baseUrl = getBaseUrl(targetService);

        try {
            if (requestBody instanceof MultipartFile) {
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultipartFile file = (MultipartFile) requestBody;

                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", fileResource);

                requestEntity = new HttpEntity<>(body, headers);
            } else {
                headers.setContentType(MediaType.APPLICATION_JSON);
                requestEntity = new HttpEntity<>(requestBody, headers);
            }
            ResponseEntity<String> response =
                    restTemplate.exchange(baseUrl + endpoint, method, requestEntity, String.class);

            if (response.getBody() == null) return null;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(response.getBody(), responseType);

        } catch (Exception e) {
            // log ra payload
            if (requestBody != null) {
                log.error("Payload: {}", requestBody);
            }
            throw new RuntimeException("Lỗi gọi Python API [" + endpoint + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public CompilerResponseDto executeCode(CompilerRequestDto request) {

        try {
            // 3. THỰC THI CALL SANG FASTAPI SANDBOX
            ResponseEntity<String> response = restTemplate.postForEntity(COMPILER_SERVICE_URL, request, String.class);

            if (response.getBody() == null) return null;

            // 4. BÓC TÁCH MAPPING KẾT QUẢ VỀ DTO
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return objectMapper.readValue(response.getBody(), CompilerResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Compiler Service: " + e.getMessage(), e);
        }
    }

    @Override
    public WebSocket connectWebSocket(
            PythonService targetService, String endpoint, Consumer<String> onMessage, Consumer<Throwable> onError) {

        String url = getBaseUrl(targetService).replace("http://", "ws://").replace("https://", "wss://") + endpoint;

        return HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

                        onMessage.accept(data.toString());
                        webSocket.request(1);

                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {

                        onError.accept(error);
                    }
                })
                .join();
    }
}
