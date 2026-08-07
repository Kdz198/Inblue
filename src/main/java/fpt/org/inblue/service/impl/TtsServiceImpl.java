package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.TtsService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TtsServiceImpl implements TtsService {

    private static final String REDIS_TOKEN_KEY = "elevenlabs:bearer:token";

    @Value("${elevenlabs.voice.id:}")
    private String voiceId;

    private final RestTemplate restTemplate;

    private final ApiClient apiClient;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public byte[] generateAudio(String text, String targetVoiceId) {
        String finalVoiceId = (targetVoiceId != null && !targetVoiceId.isEmpty()) ? targetVoiceId : this.voiceId;
        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + finalVoiceId;

        Map<String, Object> voiceSettings = new HashMap<>();
        voiceSettings.put("stability", 0.5);
        voiceSettings.put("similarity_boost", 0.75);

        try {
            return callElevenLabsApi(url, text, voiceSettings);
        } catch (Exception e) {
            System.err.println(
                    "[DEBUG TTS] ElevenLabs API failed (" + e.getMessage() + "). Falling back to Google TTS...");
            return fallbackToGoogleTts(text);
        }
    }

    private byte[] callElevenLabsApi(String url, String text, Map<String, Object> voiceSettings) {
        String bearerToken = stringRedisTemplate.opsForValue().get(REDIS_TOKEN_KEY);

        if (bearerToken == null || bearerToken.isEmpty()) {
            System.out.println("[DEBUG TTS] Không tìm thấy Token SSO trong Redis. Bỏ qua ElevenLabs.");
            throw new RuntimeException("Missing Token in Redis");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.set("Accept", "audio/mpeg");

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("model_id", "eleven_turbo_v2_5");
        body.put("voice_settings", voiceSettings);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);
        return response.getBody();
    }

    private byte[] fallbackToGoogleTts(String text) {
        String googleTtsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=vi&client=tw-ob&q={text}";
        ResponseEntity<byte[]> fallbackResponse = restTemplate.getForEntity(googleTtsUrl, byte[].class, text);
        return fallbackResponse.getBody();
    }

    @Override
    public String enhancedTranscript(@RequestBody EnhanceTranscriptRequest request) {
        return apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.ENHANCE_TRANSCRIPT, request, "transcript", true, null, String.class);
    }
}
