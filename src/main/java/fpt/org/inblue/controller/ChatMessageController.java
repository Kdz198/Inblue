package fpt.org.inblue.controller;

import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")

public class ChatMessageController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/{currentFullId}/{recipientFullId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String currentFullId,
            @PathVariable String recipientFullId) {
        List<ChatMessage> history = chatService.getChatHistory(currentFullId, recipientFullId);
        return ResponseEntity.ok(history);
    }
}
