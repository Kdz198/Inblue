package fpt.org.inblue.service;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;

import java.net.http.WebSocket;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

public interface ApiClient {
    <T> T callApi(
            PythonService targetService, String endpoint, HttpMethod method, Object requestBody, Class<T> responseType);

    <T> T sendChatToAnythingLlm(
            AnythingLlmWorkspace workspace,
            Object payload, // Đổi từ String sang Object
            String sessionId,
            boolean reset,
            List<MultipartFile> files,
            Class<T> responseType);

    CompilerResponseDto executeCode(CompilerRequestDto request);
    WebSocket connectWebSocket(
            PythonService targetService,
            String endpoint,
            Consumer<String> onMessage,
            Consumer<Throwable> onError
    );
}
