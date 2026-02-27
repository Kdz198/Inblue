package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.service.InterviewProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interview")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InterviewProcessController {

    private final InterviewProcessService interviewService;

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
}