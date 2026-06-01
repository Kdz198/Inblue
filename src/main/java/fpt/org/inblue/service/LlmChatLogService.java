package fpt.org.inblue.service;

import fpt.org.inblue.model.LlmChatLog;
import fpt.org.inblue.repository.LlmChatLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmChatLogService {

    private final LlmChatLogRepository repository;

    @Async
    public void saveLog(String traceId, String sessionId, String workspace,
                        String userMessage, String aiResponse,
                        Integer promptTokens, Integer completionTokens) {
        try {
            LlmChatLog chatLog = LlmChatLog.builder()
                    .traceId(traceId)
                    .sessionId(sessionId)
                    .workspace(workspace)
                    .userMessage(userMessage)
                    .aiResponse(aiResponse)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .build();

            repository.save(chatLog);

        } catch (Exception e) {
            log.error("✘ Lỗi khi lưu LlmChatLog xuống DB: {}", e.getMessage());
        }
    }
}