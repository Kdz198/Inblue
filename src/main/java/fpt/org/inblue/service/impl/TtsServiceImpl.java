package fpt.org.inblue.service.impl;

import fpt.org.inblue.service.TtsService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TtsServiceImpl implements TtsService {

    @Value("${elevenlabs.api.key:}")
    private String apiKey;

    @Value("${elevenlabs.bearer.token:}")
    private String bearerToken;

    @Value("${elevenlabs.voice.id:}")
    private String voiceId;

    private final RestTemplate restTemplate;

    public TtsServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public byte[] generateAudio(String text) {
        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (bearerToken != null && !bearerToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + bearerToken);
        } else {
            headers.set("xi-api-key", apiKey);
        }

        headers.set("Accept", "audio/mpeg");

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("model_id", "eleven_turbo_v2_5");

        Map<String, Object> voiceSettings = new HashMap<>();
        voiceSettings.put("stability", 0.5);
        voiceSettings.put("similarity_boost", 0.75);
        body.put("voice_settings", voiceSettings);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("ElevenLabs API failed (" + e.getMessage() + "). Falling back to Google TTS...");
            // Fallback to Google TTS (Free, no API key required)
            String googleTtsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=vi&client=tw-ob&q={text}";
            ResponseEntity<byte[]> fallbackResponse = restTemplate.getForEntity(googleTtsUrl, byte[].class, text);
            return fallbackResponse.getBody();
        }
    }
}
