package fpt.org.inblue.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String contentType = request.getContentType();
        // Bỏ qua, không cache body nếu là upload file
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            log.info("▶ Nhận API (Upload File): [{} {}]", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Sử dụng Wrapper tự chế để ép đọc trước toàn bộ Body vào bộ nhớ cache
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String requestBody = new String(cachedRequest.getCachedBody(), StandardCharsets.UTF_8);

        // 2. IN LOG NGAY TẠI ĐÂY (Đúng thứ tự request vừa đặt chân tới hệ thống)
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

        // 3. Cho đi tiếp vào Controller xử lý (Controller vẫn đọc body bình thường từ mảng byte đã cache)
        filterChain.doFilter(cachedRequest, response);
    }

    /**
     * Lớp Wrapper tự chế giúp đọc và lưu lại Request Body (Eager Caching)
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            // Đọc toàn bộ bytes từ input stream gốc ngay khi khởi tạo
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        public byte[] getCachedBody() {
            return this.cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() { return byteArrayInputStream.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(ReadListener readListener) {}
                @Override
                public int read() { return byteArrayInputStream.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}