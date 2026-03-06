package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.ChatDto;
import fpt.org.inblue.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller("api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @MessageMapping
    public void processMessage(@Payload ChatDto message) {
        chatService.processMessage(message);
    }
}
