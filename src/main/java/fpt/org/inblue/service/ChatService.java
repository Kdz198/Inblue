package fpt.org.inblue.service;

import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.model.dto.ChatDto;
import fpt.org.inblue.model.enums.Role;

import java.util.List;

public interface ChatService {
    void processMessage(ChatDto message);
    List<ChatMessage> getChatHistory(String senderId, String recipientId);
    List<Integer> findAllContact(int myId, Role role);
}
