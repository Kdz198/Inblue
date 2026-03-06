package fpt.org.inblue.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //khi client muốn chat thì sẽ gửi request http đến endpoint /ws-chat sau đó server sẽ nâng cấp kết nối này từ http -> WebSocket protocol
        //sau khi thành websocket protocol thì cái cổng này sẽ biến thành 1 ống dẫn dữ liệu liên tục
        registry.addEndpoint("/ws-chat")
                //đây là cái cổng, ai muốn chat thì sẽ kết nối vào cổng này
                .setAllowedOriginPatterns("*")
                .withSockJS();
       // Một số trình duyệt cũ hoặc môi trường mạng bị chặn WebSocket sẽ dùng cái này để tự động chuyển sang kiểu truyền tải khác (như Long Polling) để đảm bảo không bị mất kết nối.
        }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/user");
        //néu tin nhắn có địa chỉ bắt đầu bằng /topic hoac /user thì sẽ được gửi đến message broker (hệ thống bưu điện nội bộ của Spring)
        //topic sẽ dùng cho tin nhắn nhóm và user sẽ dùng cho tin nhắn riêng tư
        registry.setUserDestinationPrefix("/user");
        //Là hệ thống Hộp thư cá nhân. Nó giúp nhân viên bưu điện (Spring) biết rằng tin nhắn này phải bỏ vào đúng hộp thư có tên người nhận, chứ không được đọc oang oang trên loa.
    }
}
