package fpt.org.inblue.controller;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")

@RequiredArgsConstructor

public class ChatMessageController {
    private final ChatService chatService;

    @GetMapping("/{currentFullId}/{recipientFullId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String currentFullId,
            @PathVariable String recipientFullId) {
        List<ChatMessage> history = chatService.getChatHistory(currentFullId, recipientFullId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<Integer>> getContacts(@RequestParam int myId, @RequestParam Role role) {
        List<Integer> contacts = chatService.findAllContact(myId,role);
        return ResponseEntity.ok(contacts);}
}
