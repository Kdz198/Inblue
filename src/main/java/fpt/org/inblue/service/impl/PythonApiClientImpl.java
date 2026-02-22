package fpt.org.inblue.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.service.PythonApiClient;
import lombok.RequiredArgsConstructor;
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
public class PythonApiClientImpl implements PythonApiClient {

    private final RestTemplate restTemplate;

    @Value("${PYTHON_LLM_URL:}")
    private String LLM_BASE_URL;

    @Value("${PYTHON_VISION_URL:}")
    private String VISION_BASE_URL;

    private String getBaseUrl(PythonService targetService) {
        return targetService == PythonService.VISION ? VISION_BASE_URL : LLM_BASE_URL;
    }

    @Override
    public <T> T callApi( PythonService targetService, String endpoint, HttpMethod method, Object requestBody, Class<T> responseType) {
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
            }
            else {
                headers.setContentType(MediaType.APPLICATION_JSON);
                requestEntity = new HttpEntity<>(requestBody, headers);
            }ResponseEntity<String> response = restTemplate.exchange(
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
