package fpt.org.inblue.controller;

import fpt.org.inblue.service.SpeechService;
import java.io.IOException;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SttWebSocketController extends BinaryWebSocketHandler {

    private final SpeechService speechService;

    /**
     * Client WebSocket session
     *      ↓
     * Python/Gemini WebSocket session
     */
    private final Map<String, WebSocket> sttSessions =
            new ConcurrentHashMap<>();

    /**
     * FE vừa connect vào Java.
     */
    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        log.info(
                "[STT] Client connected: {}",
                session.getId()
        );

        WebSocket sttSocket = speechService.connectStt(
                message -> sendToClient(session, message),

                error -> log.error(
                        "[STT] Error session {}",
                        session.getId(),
                        error
                )
        );

        sttSessions.put(
                session.getId(),
                sttSocket
        );
    }

    /**
     * FE gửi PCM chunk tới Java.
     *
     * Java forward ngay sang Python/Gemini.
     */
    @Override
    protected void handleBinaryMessage(
            WebSocketSession session,
            BinaryMessage message) {

        WebSocket sttSocket =
                sttSessions.get(session.getId());

        if (sttSocket == null) {
            return;
        }

        sttSocket.sendBinary(
                message.getPayload(),
                true
        );
    }

    /**
     * Dùng cho control message:
     *
     * {"type":"audio_end"}
     */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) {

        WebSocket sttSocket =
                sttSessions.get(session.getId());

        if (sttSocket == null) {
            return;
        }

        sttSocket.sendText(
                message.getPayload(),
                true
        );
    }

    /**
     * FE disconnect → đóng luôn connection Python.
     */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        WebSocket sttSocket =
                sttSessions.remove(session.getId());

        if (sttSocket != null) {
            sttSocket.sendClose(
                    WebSocket.NORMAL_CLOSURE,
                    "Client disconnected"
            );
        }

        log.info(
                "[STT] Client disconnected: {}",
                session.getId()
        );
    }

    private void sendToClient(
            WebSocketSession session,
            String message) {

        if (!session.isOpen()) {
            return;
        }

        try {
            session.sendMessage(
                    new TextMessage(message)
            );
        } catch (IOException e) {
            log.error(
                    "[STT] Cannot send transcript to client {}",
                    session.getId(),
                    e
            );
        }
    }
}