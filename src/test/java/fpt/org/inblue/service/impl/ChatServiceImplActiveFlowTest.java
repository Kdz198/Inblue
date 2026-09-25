package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.Role;
import fpt.org.inblue.model.ChatMessage;
import fpt.org.inblue.model.dto.ChatDto;
import fpt.org.inblue.repository.ChatMessageRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplActiveFlowTest {
    @Mock
    ChatMessageRepository repository;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChatServiceImpl(repository, messagingTemplate);
    }

    @Test
    void parseIdAndTypeHandleValidParticipantKey() {
        assertEquals(7, service.parseId("USER_7"));
        assertEquals("USER", service.getType("USER_7"));
    }

    @Test
    void parseIdAndTypeHandleMalformedParticipantKey() {
        assertEquals(0, service.parseId("invalid"));
        assertNull(service.getType(null));
        assertThrows(NumberFormatException.class, () -> service.parseId("USER_x"));
    }

    @Test
    void getHistoryParsesBothParticipantKeys() {
        ChatMessage message = new ChatMessage();
        when(repository.getHistory(7, "USER", 3, "MENTOR")).thenReturn(List.of(message));
        assertEquals(List.of(message), service.getChatHistory("USER_7", "MENTOR_3"));
    }

    @Test
    void findAllContactUsesRoleName() {
        when(repository.findPartnerIds(7, "USER")).thenReturn(List.of(3, 4));
        assertEquals(List.of(3, 4), service.findAllContact(7, Role.USER));
    }

    @Test
    void processMessagePersistsAndPublishesTypedMessage() {
        ChatDto dto = new ChatDto();
        dto.setSenderId("USER_7");
        dto.setRecipientId("MENTOR_3");
        dto.setContent("Hello");
        service.processMessage(dto);
        verify(repository)
                .save(org.mockito.ArgumentMatchers.argThat(message -> message.getSenderId() == 7
                        && message.getRecipientId() == 3
                        && "Hello".equals(message.getContent())));
        verify(messagingTemplate)
                .convertAndSendToUser(
                        org.mockito.ArgumentMatchers.eq("MENTOR_3"),
                        org.mockito.ArgumentMatchers.eq("queue/messages"),
                        org.mockito.ArgumentMatchers.any(ChatMessage.class));
    }
}
