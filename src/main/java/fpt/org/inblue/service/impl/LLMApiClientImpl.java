package fpt.org.inblue.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.service.LLMApiClient;
import fpt.org.inblue.service.LlmChatLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LLMApiClientImpl implements LLMApiClient {

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

    private final LlmChatLogService chatLogService;


    private String getBaseUrl(PythonService targetService) {
        return targetService == PythonService.VISION ? VISION_BASE_URL : LLM_BASE_URL;
    }


    // =========================================================================
    // HÀM MỚI: GỌI CHAT CÓ ĐÍNH KÈM FILE (DIRECT CONTEXT INJECTION)
    // =========================================================================
    public <T> T sendChatToAnythingLlm(
            AnythingLlmWorkspace workspace,
            String message,
            String sessionId,
            boolean reset,
            List<MultipartFile> files,
            Class<T> responseType) {

        String endpoint = "/workspace/" + workspace.getSlug() + "/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ANYTHING_LLM_API_KEY);

        // Build Payload JSON
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("mode", "chat");
        requestBody.put("sessionId", sessionId);
        requestBody.put("reset", reset);

        // Xử lý động mảng file (nếu có)
        if (files != null && !files.isEmpty()) {
            List<Map<String, String>> attachments = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    String originalName = file.getOriginalFilename();
                    String realMimeType = file.getContentType();
                    if (realMimeType == null) {
                        realMimeType = "application/octet-stream";
                    }

                    // LÕI XỬ LÝ MIME THEO LUẬT CỦA ANYTHING LLM:
                    // - Ảnh: Giữ nguyên MIME thật (VD: image/png).
                    // - Document (PDF, Word, Txt): Bắt buộc gán payloadMimeType là "application/anythingllm-document".
                    String payloadMimeType = realMimeType.startsWith("image/")
                            ? realMimeType
                            : "application/anythingllm-document";

                    // Chuyển file sang Base64
                    byte[] fileBytes = file.getBytes();
                    String base64String = Base64.getEncoder().encodeToString(fileBytes);

                    // Nối Data URI Prefix (luôn dùng realMimeType ở prefix)
                    String contentString = "data:" + realMimeType + ";base64," + base64String;

                    Map<String, String> attachment = new HashMap<>();
                    attachment.put("name", originalName);
                    attachment.put("mime", payloadMimeType);
                    attachment.put("contentString", contentString);

                    attachments.add(attachment);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi encode file [" + file.getOriginalFilename() + "]: " + e.getMessage(), e);
                }
            }
            requestBody.put("attachments", attachments);
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    ANYTHING_LLM_URL + endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getBody() == null) return null;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // 1. Đọc toàn bộ JSON Response
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String textResponse = rootNode.path("textResponse").asText();

            // 2. Bóc tách Metrics để lấy thông tin Token
            JsonNode metricsNode = rootNode.path("metrics");
            Integer promptTokens = metricsNode.path("prompt_tokens").asInt(0);
            Integer completionTokens = metricsNode.path("completion_tokens").asInt(0);

            // 3. Lấy TraceID (Nếu bạn đang dùng MDC trong Filter, lấy ra bằng cách này.
            // Nếu chưa dùng MDC, có thể tạm gán là UUID hoặc pass từ tham số hàm vào)
            String traceId = org.slf4j.MDC.get("traceId");
            if (traceId == null) {
                traceId = "no-trace-id";
            }

            // 4. GỌI HÀM LƯU LOG (Nó sẽ chạy ngầm không làm chậm API)
            chatLogService.saveLog(
                    traceId,
                    sessionId,
                    workspace.name(), // Lấy tên Enum làm String
                    message,
                    textResponse,
                    promptTokens,
                    completionTokens
            );

            if (textResponse == null || textResponse.trim().isEmpty()) {
                return null;
            }

            // 5. Trả về kết quả cho Controller như bình thường
            return objectMapper.readValue(textResponse, responseType);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Chat AnythingLLM [" + endpoint + "]: " + e.getMessage(), e);
        }

    }

    @Override
    public <T> T callApi(PythonService targetService, String endpoint, HttpMethod method, Object requestBody, Class<T> responseType) {
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
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + endpoint,
                    method,
                    requestEntity,
                    String.class
            );

            if (response.getBody() == null) return null;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(response.getBody(), responseType);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Python API [" + endpoint + "]: " + e.getMessage(), e);
        }
    }
}
