package fpt.org.inblue.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Chỉ bọc Request lại để giữ được luồng data (Stream)
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 10240);
        // 2. Cho request đi tiếp vào Controller xử lý
        // LƯU Ý QUAN TRỌNG: Phải cho đi qua filterChain TRƯỚC,
        // thì Controller mới "đọc" data, lúc đó cái wrapper mới cache lại được body.
        filterChain.doFilter(requestWrapper, response);

// 3. Lấy Request Body ra (Dành cho raw JSON/XML)
        String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        // 4. In log (TraceID tự động dính kèm)
        if (!requestBody.isBlank()) {
            // Nén JSON lại thành 1 dòng cho dễ nhìn
            String compactBody = requestBody.replaceAll("[\\r\\n]+", " ");
            log.info("▶ Nhận API: [{} {}] - Payload JSON: {}",
                    request.getMethod(), request.getRequestURI(), compactBody);
        } else {
            // Nếu raw rỗng, vớt vát xem có phải gửi dạng Form-Data không
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.isEmpty()) {
                StringBuilder formParams = new StringBuilder();
                parameterMap.forEach((key, value) ->
                        formParams.append(key).append("=").append(String.join(",", value)).append(" | "));

                log.info("▶ Nhận API: [{} {}] - Payload Form-Data: {}",
                        request.getMethod(), request.getRequestURI(), formParams);
            } else {
                log.info("▶ Nhận API: [{} {}] - (Không có Request Body)",
                        request.getMethod(), request.getRequestURI());
            }
        }
    }
}