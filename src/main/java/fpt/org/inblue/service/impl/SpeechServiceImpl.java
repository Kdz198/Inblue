package fpt.org.inblue.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.SpeechService;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SpeechServiceImpl implements SpeechService {

    private static final String REDIS_TOKEN_KEY = "elevenlabs:bearer:token";

    @Value("${elevenlabs.voice.id:}")
    private String voiceId;

    private final RestTemplate restTemplate;

    private final ApiClient apiClient;

    private final StringRedisTemplate stringRedisTemplate;

    private static final int GOOGLE_TTS_MAX_CHARS = 180;

    @Value("${PYTHON_LLM_URL:}")
    private String LLM_BASE_URL;

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

    @Override
    public String enhancedTranscript(@RequestBody EnhanceTranscriptRequest request) {
        return apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.ENHANCE_TRANSCRIPT, request, "transcript", true, null, String.class);
    }

    @Override
    public byte[] generateAudioFromPython(String text, String voice) {
        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("voice", voice);

        return apiClient.callApi(PythonService.LLM, "/api/v1/tts", HttpMethod.POST, body, byte[].class);
    }

    @Override
    public void streamAudioFromPython(String text, String voice, OutputStream outputStream) {
        String url = LLM_BASE_URL + "/api/v1/tts/stream";

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("voice", voice);

        restTemplate.execute(
                url,
                HttpMethod.POST,
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getHeaders().setAccept(List.of(MediaType.ALL));
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(request.getBody(), body);
                },
                response -> {
                    System.out.println("[DEBUG TTS] Streaming audio from Python service...");
                    StreamUtils.copy(response.getBody(), outputStream);
                    return null;
                });
    }

    @Override
    public WebSocket connectStt(Consumer<String> onMessage, Consumer<Throwable> onError) {

        WebSocket socket =
                apiClient.connectWebSocket(PythonService.LLM, "/api/v1/transcription/live", onMessage, onError);

        socket.sendText(
                """
                        {"type":"start","sampleRate":16000}
                        """, true);

        return socket;
    }

    private byte[] fallbackToGoogleTts(String text) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            for (String chunk : splitTextForGoogleTts(text, GOOGLE_TTS_MAX_CHARS)) {
                URI uri = UriComponentsBuilder.fromUriString("https://translate.google.com/translate_tts")
                        .queryParam("ie", "UTF-8")
                        .queryParam("tl", "vi")
                        .queryParam("client", "tw-ob")
                        .queryParam("q", chunk)
                        .build()
                        .encode(StandardCharsets.UTF_8)
                        .toUri();

                ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);

                if (response.getBody() != null) {
                    output.write(response.getBody());
                }
            }

            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Google TTS fallback failed", e);
        }
    }

    private List<String> splitTextForGoogleTts(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        String normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();

        if (normalized.isBlank()) {
            return chunks;
        }

        List<String> sentences = splitIntoSentences(normalized);

        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (sentence.length() > maxChars) {
                flushChunk(chunks, current);

                chunks.addAll(splitLongSentence(sentence, maxChars));
                continue;
            }

            int nextLength = current.length() == 0 ? sentence.length() : current.length() + 1 + sentence.length();

            if (nextLength <= maxChars) {
                if (current.length() > 0) {
                    current.append(" ");
                }
                current.append(sentence);
            } else {
                flushChunk(chunks, current);
                current.append(sentence);
            }
        }

        flushChunk(chunks, current);
        return chunks;
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            current.append(ch);

            boolean isSentenceEnd = ch == '.' || ch == '?' || ch == '!' || ch == '。';
            boolean nextIsBoundary = i == text.length() - 1 || Character.isWhitespace(text.charAt(i + 1));

            if (isSentenceEnd && nextIsBoundary) {
                flushChunk(sentences, current);
            }
        }

        flushChunk(sentences, current);
        return sentences;
    }

    private List<String> splitLongSentence(String sentence, int maxChars) {
        List<String> chunks = new ArrayList<>();
        String remaining = sentence.trim();

        while (remaining.length() > maxChars) {
            int splitAt = findBestSplitIndex(remaining, maxChars);

            chunks.add(remaining.substring(0, splitAt).trim());
            remaining = remaining.substring(splitAt).trim();
        }

        if (!remaining.isBlank()) {
            chunks.add(remaining);
        }

        return chunks;
    }

    private int findBestSplitIndex(String text, int maxChars) {
        int splitAt = -1;

        // Ưu tiên băm nhẹ theo dấu phẩy / chấm phẩy / hai chấm trước.
        char[] softBreaks = {',', ';', ':'};
        for (char softBreak : softBreaks) {
            int idx = text.lastIndexOf(softBreak, maxChars);
            if (idx > splitAt) {
                splitAt = idx + 1;
            }
        }

        // Nếu không có dấu câu phụ thì băm theo khoảng trắng.
        if (splitAt <= 0) {
            splitAt = text.lastIndexOf(' ', maxChars);
        }

        // Bí quá mới băm giữa chữ.
        if (splitAt <= 0) {
            splitAt = maxChars;
        }

        return splitAt;
    }

    private void flushChunk(List<String> chunks, StringBuilder current) {
        String chunk = current.toString().trim();
        if (!chunk.isBlank()) {
            chunks.add(chunk);
        }
        current.setLength(0);
    }
}
