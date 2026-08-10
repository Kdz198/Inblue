package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.service.InterviewProcessService;
import fpt.org.inblue.service.SpeechService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interview")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InterviewProcessController {

    private final InterviewProcessService interviewService;
    private final SpeechService speechService;

    // FE gọi cái này ngay khi vào màn hình Chat
    @GetMapping("/start/{sessionKey}")
    public QuestionResponse startInterview(@PathVariable String sessionKey) {
        return interviewService.getCurrentQuestion(sessionKey);
    }

    // FE gọi cái này khi bấm nút Gửi
    @PostMapping("/submit")
    public QuestionResponse submitAnswer(@RequestBody SubmitAnswerRequest request) {
        return interviewService.submitAnswer(request);
    }

    public record TtsRequest(String text, String voiceId) {}

    @PostMapping("/tts")
    public ResponseEntity<byte[]> generateAudio(@RequestBody TtsRequest request) {
        byte[] audio = speechService.generateAudio(request.text(), request.voiceId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).body(audio);
    }

    @PostMapping("/enhance-transcript")
    public String enhanceTranscript(@RequestBody EnhanceTranscriptRequest request) {
        return speechService.enhancedTranscript(request);
    }

    public record VoiceResponse(String id, String name, String description, String previewUrl) {}

    @GetMapping("/voices")
    public ResponseEntity<List<VoiceResponse>> getAvailableVoices() {
        // FE sẽ đọc các file MP3 tĩnh trong thư mục src/main/resources/static/voices/
        List<VoiceResponse> voices = List.of(
                new VoiceResponse(
                        "UsgbMVmY3U59ijwK5mdh",
                        "Trieu Duong (Male)",
                        "Giọng nam truyền cảm, phù hợp đọc Podcast và kể chuyện",
                        "/voices/trieuduong.mp3"),
                new VoiceResponse(
                        "x4KAhuXs2G8TfK9Zr7Q4",
                        "Cam Hong (Female)",
                        "Giọng nữ thanh thoát, chuyên nghiệp, chuẩn phong cách TVC",
                        "/voices/camhong.mp3"),
                new VoiceResponse(
                        "f5q6kePPoQAjCPYG6moa",
                        "Giang (Female)",
                        "Giọng nữ ấm áp, tự nhiên, thích hợp dẫn chương trình",
                        "/voices/giang.mp3"),
                new VoiceResponse(
                        "CxJbDdwqY48MY3gPVYwe",
                        "Trinh (Male)",
                        "Giọng nam trầm tĩnh, đọc chậm rãi và dứt khoát",
                        "/voices/trinh.mp3"));
        return ResponseEntity.ok(voices);
    }
}
