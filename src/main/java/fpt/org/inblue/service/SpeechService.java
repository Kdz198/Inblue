package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import java.io.OutputStream;
import java.net.http.WebSocket;
import java.util.function.Consumer;

public interface SpeechService {
    byte[] generateAudio(String text, String voiceId);

    byte[] generateAudioFromPython(String text, String voice);

    void streamAudioFromPython(String text, String voice, OutputStream outputStream);

    String enhancedTranscript(EnhanceTranscriptRequest request);

    WebSocket connectStt(Consumer<String> onMessage, Consumer<Throwable> onError);
}
