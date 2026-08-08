package fpt.org.inblue.Config;

import fpt.org.inblue.controller.SttWebSocketController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SttWebSocketConfig
        implements WebSocketConfigurer {

    private final SttWebSocketController sttWebSocketController;

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        registry.addHandler(
                        sttWebSocketController,
                        "/api/v1/interview/transcribe"
                )
                .setAllowedOriginPatterns("*");
    }
}