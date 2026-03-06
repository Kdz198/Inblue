package fpt.org.inblue.model.dto;

import lombok.Data;

@Data
public class ChatDto {
    private String senderId;
    private String recipientId;
    private String content;
}
