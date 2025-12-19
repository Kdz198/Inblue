package fpt.org.inblue.controller;

import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.JoinSessionDtoRequest;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;
import fpt.org.inblue.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sessions")
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

    @GetMapping("/{userId}/by-user")
    public ResponseEntity<List<Session>> getSessionsByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<Session> updateSession( @RequestBody Session session) {
        return ResponseEntity.ok(sessionService.updateSession(session));
    }

    @PostMapping("create-session")
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionCreationRequest sessionCreationRequest) {
        return ResponseEntity.ok(sessionService.createSession(sessionCreationRequest));
    }

    @PostMapping("join-session")
    public ResponseEntity<Void> saveJoinRecord(@RequestBody JoinSessionDtoRequest request) {
        sessionService.saveJoinRecord(request);
        return ResponseEntity.ok().build();
    }
}
