package fpt.org.inblue.controller;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.model.dto.ChatDto;
import fpt.org.inblue.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatDto message) {
        chatService.processMessage(message);
    }
}
