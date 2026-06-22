package fpt.org.inblue.model.dto.request;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequest {
    private Long applicationId;
    private String textContent;

    @Nullable
    private MultipartFile file;

    // Dành riêng cho vòng QUIZ
    private List<String> quizAnswers;
    private List<CompileRequest> compileRequest;

    /**
     * Setter cho trường hợp gửi một JSON string duy nhất (object hoặc array).
     * Spring MVC gọi setter này khi chỉ có 1 giá trị cho tham số "compileRequest".
     *
     * Cách gửi (1 object):
     * {"problemId":1,"language":"JAVA","sourceCode":"...","test":true}
     * Cách gửi (array): [{"problemId":1,...},{"problemId":2,...}]
     */
    public void setCompileRequest(String compileRequestJson) {
        if (compileRequestJson == null || compileRequestJson.trim().isEmpty()
                || compileRequestJson.equals("[object Object]")) {
            this.compileRequest = null;
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String trimmed = compileRequestJson.trim();
            if (trimmed.startsWith("[")) {
                this.compileRequest = objectMapper.readValue(trimmed, new TypeReference<List<CompileRequest>>() {
                });
            } else {
                CompileRequest single = objectMapper.readValue(trimmed, CompileRequest.class);
                this.compileRequest = new java.util.ArrayList<>();
                this.compileRequest.add(single);
            }
            if (this.compileRequest != null && this.compileRequest.isEmpty()) {
                this.compileRequest = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse compileRequest (String) trong SubmitRequest: " + e.getMessage());
            this.compileRequest = null;
        }
    }

    /**
     * Setter cho trường hợp gửi nhiều giá trị "compileRequest" trong cùng một
     * request.
     * Spring MVC gọi setter này khi có từ 2 giá trị trở lên cho cùng tham số
     * "compileRequest".
     *
     * Hỗ trợ 2 trường hợp:
     * - Frontend JS gửi đúng: mỗi phần tử là một JSON object hoàn chỉnh
     * - Swagger UI gửi: JSON bị tách tại các dấu phẩy → tự động reassemble lại
     */
    public void setCompileRequest(List<String> compileRequestJsons) {
        if (compileRequestJsons == null || compileRequestJsons.isEmpty()) {
            this.compileRequest = null;
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        // === Bước 1: Thử parse từng phần tử như JSON hoàn chỉnh (cách dùng từ frontend
        // JS) ===
        // Cách gửi đúng: mỗi compileRequest field là một JSON string hoàn chỉnh
        // compileRequest =
        // {"problemId":1,"language":"JAVA","sourceCode":"...","test":true}
        // compileRequest =
        // {"problemId":2,"language":"JAVA","sourceCode":"...","test":true}
        List<CompileRequest> result = new java.util.ArrayList<>();
        boolean hasParseError = false;
        for (String json : compileRequestJsons) {
            if (json == null || json.trim().isEmpty() || json.equals("[object Object]"))
                continue;
            String trimmed = json.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                // Nếu có phần tử không phải JSON → biết chắc là bị tách bởi Swagger
                hasParseError = true;
                break;
            }
            try {
                if (trimmed.startsWith("[")) {
                    result.addAll(objectMapper.readValue(trimmed, new TypeReference<List<CompileRequest>>() {
                    }));
                } else {
                    result.add(objectMapper.readValue(trimmed, CompileRequest.class));
                }
            } catch (Exception e) {
                // Parse thất bại → có thể là JSON bị cắt giữa chừng
                hasParseError = true;
                break;
            }
        }

        if (!hasParseError && !result.isEmpty()) {
            this.compileRequest = result;
            return;
        }

        // === Bước 2: Reassemble — join tất cả các mảnh bằng "," để khôi phục JSON gốc
        // ===
        // Swagger UI tách JSON tại từng dấu phẩy (kể cả dấu phẩy trong string value).
        // Khi join lại, dấu phẩy được khôi phục đúng vị trí → JSON hợp lệ trở lại.
        try {
            String joined = String.join(",", compileRequestJsons).trim();
            if (joined.startsWith("[")) {
                this.compileRequest = objectMapper.readValue(joined, new TypeReference<List<CompileRequest>>() {
                });
            } else if (joined.startsWith("{")) {
                // Một hoặc nhiều object liền kề: bọc vào array để parse
                String asArray = "[" + joined + "]";
                this.compileRequest = objectMapper.readValue(asArray, new TypeReference<List<CompileRequest>>() {
                });
            } else {
                System.err.println("compileRequest không phải JSON hợp lệ sau khi reassemble: "
                        + joined.substring(0, Math.min(100, joined.length())));
                this.compileRequest = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse compileRequest (List) sau khi reassemble: " + e.getMessage());
            this.compileRequest = null;
        }

        if (this.compileRequest != null && this.compileRequest.isEmpty()) {
            this.compileRequest = null;
        }
    }
}
