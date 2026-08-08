package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;

import java.net.http.WebSocket;
import java.util.function.Consumer;

public interface SpeechService {
    byte[] generateAudio(String text, String voiceId);

    String enhancedTranscript(EnhanceTranscriptRequest request);

    WebSocket connectStt(
            Consumer<String> onMessage,
            Consumer<Throwable> onError
    );
}
