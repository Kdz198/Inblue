package fpt.org.inblue.model.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
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
    private CompileRequest compileRequest;
    /**
     * TRUNG TÂM XỬ LÝ: Custom Setter để tự động parse String JSON sang Object
     * Khi đi qua @ModelAttribute, Spring thấy tham số gửi lên dạng String,
     * nó sẽ ưu tiên tìm hàm setCompileRequest nhận vào một chuỗi String này.
     */
    public void setCompileRequest(String compileRequestJson) {
        if (compileRequestJson == null || compileRequestJson.trim().isEmpty() || compileRequestJson.equals("[object Object]")) {
            this.compileRequest = null;
            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Tự động ép chuỗi JSON thô thành Object thực tế
            this.compileRequest = objectMapper.readValue(compileRequestJson, CompileRequest.class);
        } catch (Exception e) {
            // Log lỗi nếu chuỗi JSON truyền lên bị sai cú pháp
            System.err.println("Lỗi parse compileRequest trong SubmitRequest: " + e.getMessage());
            this.compileRequest = null;
        }
    }
}
