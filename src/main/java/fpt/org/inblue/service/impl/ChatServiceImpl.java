package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.model.dto.ChatDto;
import fpt.org.inblue.repository.ChatMessageRepository;
import fpt.org.inblue.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void processMessage(ChatDto message) {
        System.out.println("Received message: " + message.toString());
        int senderId = parseId(message.getSenderId());
        int recipientId = parseId(message.getRecipientId());
        String senderType = getType(message.getSenderId());
        String recipientType = getType(message.getRecipientId());
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setSenderType(senderType);
        chatMessage.setRecipientId(recipientId);
        chatMessage.setRecipientType(recipientType);
        chatMessage.setContent(message.getContent());
        chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId(),
                "queue/messages",
                chatMessage
        );
    }

    @Override
    public List<ChatMessage> getChatHistory(String senderId, String recipientId) {
        int sId = parseId(senderId);
        String sType = getType(senderId);
        int rId = parseId(recipientId);
        String rType = getType(recipientId);

        List<ChatMessage> history = chatMessageRepository.getHistory(
                sId, sType,
                rId, rType
        );
        return history;

    }

    public int parseId(String id) {
        if (id == null || !id.contains("_")) return 0;
        // Tách chuỗi theo dấu "_" và lấy phần tử thứ 2
        String idPart = id.substring(id.indexOf("_") + 1);
        return Integer.parseInt(idPart);
    }
    public String getType(String id) {
        if (id == null || !id.contains("_")) return null;
        return id.split("_")[0]; // Trả về "USER" hoặc "STAFF"
    }
}
