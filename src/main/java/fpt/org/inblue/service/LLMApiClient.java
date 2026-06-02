package fpt.org.inblue.service;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LLMApiClient {
    <T> T callApi(PythonService targetService, String endpoint, HttpMethod method, Object requestBody, Class<T> responseType);

    <T> T sendChatToAnythingLlm(
            AnythingLlmWorkspace workspace,
            Object payload, // Đổi từ String sang Object
            String sessionId,
            boolean reset,
            List<MultipartFile> files,
            Class<T> responseType);
}
