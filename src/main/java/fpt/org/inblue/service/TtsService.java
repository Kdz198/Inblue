package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;

public interface TtsService {
    byte[] generateAudio(String text);

    String enhancedTranscript(EnhanceTranscriptRequest request);
}
