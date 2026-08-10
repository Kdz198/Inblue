package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import java.net.http.WebSocket;
import java.util.function.Consumer;

import java.io.OutputStream;

public interface SpeechService {
    byte[] generateAudio(String text, String voiceId);
    
    byte[] generateAudioFromPython(String text, String voice);
    
    void streamAudioFromPython(String text, String voice, OutputStream outputStream);

    String enhancedTranscript(EnhanceTranscriptRequest request);

    WebSocket connectStt(Consumer<String> onMessage, Consumer<Throwable> onError);
}
