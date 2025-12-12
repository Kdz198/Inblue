package fpt.org.inblue.controller;

import fpt.org.inblue.model.Session;
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
    public ResponseEntity<Session> getSession(int id) {
        return ResponseEntity.ok(sessionService.getSession(id));}

    @GetMapping("/{userId}/by-user")
    public ResponseEntity<List<Session>> getSessionsByUserId(int userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }
    @PostMapping
    public ResponseEntity<Session> createSession(Session session) {
        return ResponseEntity.ok(sessionService.createSession(session));
    }
    @PutMapping
    public ResponseEntity<Session> updateSession(Session session) {
        return ResponseEntity.ok(sessionService.updateSession(session));
    }

}
