package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.request.EnhanceTranscriptRequest;
import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.service.InterviewProcessService;
import fpt.org.inblue.service.TtsService;
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
    private final TtsService ttsService;

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

    public record TtsRequest(String text) {}

    @PostMapping("/tts")
    public ResponseEntity<byte[]> generateAudio(@RequestBody TtsRequest request) {
        byte[] audio = ttsService.generateAudio(request.text());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).body(audio);
    }

    @PostMapping("/enhance-transcript")
    public String enhanceTranscript(@RequestBody EnhanceTranscriptRequest request) {
        return ttsService.enhancedTranscript(request);
    }
}
