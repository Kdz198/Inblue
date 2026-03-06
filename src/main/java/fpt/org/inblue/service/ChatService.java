package fpt.org.inblue.service;

import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.model.dto.ChatDto;

import java.util.List;

public interface ChatService {
    void processMessage(ChatDto message);
    List<ChatMessage> getChatHistory(String senderId, String recipientId);
}
