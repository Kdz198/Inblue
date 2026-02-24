package fpt.org.inblue.controller;

import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.request.JoinSessionDtoRequest;
import fpt.org.inblue.model.dto.dailyco.DailyWebHookPayload;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;
import fpt.org.inblue.model.enums.SessionStatus;
import fpt.org.inblue.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<Session>> getSessions() {
        return ResponseEntity.ok(sessionService.getSessions());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable int id) {
        return ResponseEntity.ok(sessionService.getSession(id));}

    @Operation(description = "Lấy tất cả session liên quan đến userId", summary = "Lấy tất cả session của user")
    @GetMapping("/{userId}/by-user")
    public ResponseEntity<List<Session>> getSessionsByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<Session> updateSession( @RequestBody Session session) {
        return ResponseEntity.ok(sessionService.updateSession(session));
    }

    @Operation(description = "json mẫu tạo 1 session họp với mentor(privacy ,enable_recording ko cần cho chọn mà gửi ẩn là public,cloud về, name khỏi cần cho điền cứ gửi như json mẫu, còn lại thì cho người dùng chọn )", summary = "{\n" +
            "  \"dailyCoCreationRequest\": {\n" +
            "    \"name\": \"\",\n" +
            "    \"privacy\": \"public\",\n" +
            "    \"properties\": {\n" +
            "      \"max_participants\": 2,\n" +
            "      \"start_video_off\": true,\n" +
            "      \"start_audio_off\": true,\n" +
            "      \"enable_screenshare\": true,\n" +
            "      \"exp\": 120,\n" +
            "      \"enable_recording\": \"cloud\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"userId\": 1,\n" +
            "  \"mentorId\": 1\n" +
            "}")
    @PostMapping("create-session")
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionCreationRequest sessionCreationRequest) {
        return ResponseEntity.ok(sessionService.createSession(sessionCreationRequest));
    }

    @Operation(description = "Ghi nhận 1 user đã tham gia vào session", summary="SessionName gửi về là roomName trong session")
    @PostMapping("join-session")
    public ResponseEntity<Void> saveJoinRecord(@RequestBody JoinSessionDtoRequest request) {
        System.out.println("Join session request received: " + request);
        sessionService.saveJoinRecord(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "webhooks/dailyco") // Chấp nhận tất cả kiểu dữ liệu
    public ResponseEntity<Void> handleDailyCoWebhook(@RequestBody DailyWebHookPayload payload) {
        System.out.println("Received Daily.co webhook event: " + payload.getEvent());
        if("participant.left".equals(payload.getEvent())) {
            System.out.println("Handling participant.left event for payload: " + payload);
            sessionService.updateLeaveRecord(payload);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("update-status")
    public ResponseEntity<Void> updateSessionStatus(@RequestParam int sessionId, @RequestParam SessionStatus status) {
        sessionService.updateSessionStatus(sessionId, status);
        return ResponseEntity.ok().build();
    }
}
