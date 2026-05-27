package fpt.org.inblue.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class SuccessResponseHandler implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // CHỈ ÁP DỤNG cho các controller thuộc package của bạn (tránh can thiệp vào Swagger/v3/api-docs)
        String className = returnType.getDeclaringClass().getName();
        return className.startsWith("fpt.org.inblue.controller");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 1. Lấy traceId hiện tại từ MDC ra
        String traceId = MDC.get("traceId");
        if (traceId == null || body == null) {
            return body;
        }

        // 2. Bỏ qua không xử lý nếu dữ liệu trả về là chuỗi thuần hoặc file/bytes
        if (body instanceof String || body instanceof byte[]) {
            return body;
        }

        // Trường hợp toán tử: Nếu dự án của bạn CÓ DÙNG chung 1 Class BaseResponse (ví dụ: ApiResponse)
        // if (body instanceof ApiResponse) {
        //     ((ApiResponse<?>) body).setTraceId(traceId);
        //     return body;
        // }

        // Trường hợp toán tử: Codebase cũ đang trả về các Object DTO tự do, chưa có class chung:
        try {
            // A. Nếu API trả về một Danh sách (List/Set) hoặc Mảng []
            // Bản chất JSON Array [] không thể tự chứa field "traceId", bắt buộc phải bọc lại thành {}
            if (body instanceof Collection || body.getClass().isArray()) {
                Map<String, Object> wrappedResponse = new LinkedHashMap<>();
                wrappedResponse.put("traceId", traceId);
                wrappedResponse.put("data", body);
                return wrappedResponse;
                // Kết quả JSON: {"traceId": "...", "data": [...]}
            }

            // B. Nếu API trả về một Object Single DTO hoặc Map {}
            // Dùng Jackson để convert Object sang Map tạm thời, nhét traceId vào root JSON luôn mà không đổi cấu trúc cũ
            Map<String, Object> jsonMap = objectMapper.convertValue(body, LinkedHashMap.class);
            jsonMap.put("traceId", traceId);
            return jsonMap;
            // Kết quả JSON: {"id": 1, "name": "Khang", ..., "traceId": "..."}

        } catch (Exception e) {
            // Fail-safe: Nếu có lỗi biến đổi xảy ra, trả về body gốc để API không bị sập
            return body;
        }
    }
}