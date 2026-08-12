package fpt.org.inblue.schedule;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.repository.JourneySummaryRepository;
import fpt.org.inblue.service.SpeechService;
import java.io.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SummaryAudioScheduler {
    private final JourneySummaryRepository journeySummaryRepository;
    private final SpeechService speechService;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    @Value("${app.default-voice:Trúc Ly}")
    private String DEFAULT_VOICE;

    public void scheduleGenerateMissingAudio() {
        for (JourneySummary summary : journeySummaryRepository.findByAudioUrlIsNull()) {
            log.info("Generating audio for summary id={}", summary.getId());
            File tmp = null;
            try {
                String script = extractScriptContent(summary.getScript());
                tmp = File.createTempFile("summary-audio-", ".wav");

                try (OutputStream out = new FileOutputStream(tmp)) {
                    speechService.streamAudioFromPython(script, DEFAULT_VOICE, out);
                }

                String url = cloudinaryService.uploadAudio(tmp);
                summary.setAudioUrl(url);
                journeySummaryRepository.save(summary);
                log.info("Audio generated successfully for summary id={}", summary.getId());

            } catch (Exception e) {
                // Bắt MỌI lỗi (RestClientException, IOException, Cloudinary lỗi...)
                // để 1 summary lỗi không làm hỏng các summary còn lại trong batch
                log.error("Failed to generate/upload audio for summary id={}: {}", summary.getId(), e.getMessage());
            } finally {
                if (tmp != null && tmp.exists()) tmp.delete();
            }
        }
    }

    private String extractScriptContent(String rawScript) {
        if (rawScript == null || rawScript.isBlank()) {
            return rawScript;
        }
        JsonNode node = objectMapper.readTree(rawScript);
        if (node.has("script")) {
            return node.get("script").asText();
        }
        return rawScript;
    }
}
