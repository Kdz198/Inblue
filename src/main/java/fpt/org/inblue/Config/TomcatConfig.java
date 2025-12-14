package fpt.org.inblue.Config;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    // Bean này được dùng để tùy chỉnh Web Server (Tomcat)
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> parameterCustomizer() {
        return (factory) -> {
            factory.addConnectorCustomizers(connector -> {
                // Tăng giới hạn số lượng tham số POST lên một giá trị lớn hơn nhiều
                // (Ví dụ: 5000) để chứa tất cả các trường (text + file) của bạn.
                // Điều này thường là giới hạn ngầm gây ra lỗi parsing.
                connector.setMaxParameterCount(5000);

                // (Tùy chọn) Tăng lại giới hạn kích thước POST Body (thường là 2MB mặc định)
                // connector.setMaxPostSize(10 * 1024 * 1024); // 10MB
            });
        };
    }
}
