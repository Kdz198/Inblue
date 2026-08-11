package fpt.org.inblue.schedule;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.repository.JourneySummaryRepository;
import fpt.org.inblue.service.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SummaryAudioScheduler {
    private final JourneySummaryRepository journeySummaryRepository;
    private final SpeechService speechService;
    private final CloudinaryService cloudinaryService;
    @Value("${app.default-voice:Trúc Ly}")
    private String DEFAULT_VOICE;


    public void scheduleGenerateMissingAudio() {
        for (JourneySummary summary : journeySummaryRepository.findByAudioUrlIsNull()) {
            File tmp = null;
            try {
                tmp = File.createTempFile("summary-audio-", ".wav");

                try (OutputStream out = new FileOutputStream(tmp)) {
                    speechService.streamAudioFromPython(summary.getScript(), DEFAULT_VOICE, out);
                }

                String url = cloudinaryService.uploadAudio(tmp);
                summary.setAudioUrl(url);
                journeySummaryRepository.save(summary);
                log.info("Audio generated successfully for summary id={}", summary.getId());

            } catch (Exception e) {
                // Bắt MỌI lỗi (RestClientException, IOException, Cloudinary lỗi...)
                // để 1 summary lỗi không làm hỏng các summary còn lại trong batch
                log.error("Failed to generate/upload audio for summary id={}: {}",
                        summary.getId(), e.getMessage());
            } finally {
                if (tmp != null && tmp.exists()) tmp.delete();
            }
        }
    }

}
