package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CodeReviewProblemGenerateRequest {
    private String topic; // Chủ đề code cần review (VD: Spring Boot Security, SQL Injection, Optimize Loop)
    private String difficulty; // EASY, MEDIUM, HARD
    private String targetLevel; // Trình độ ứng viên hướng tới
    private String programmingLanguage; // Ngôn ngữ lập trình chính (Java, Python, Javascript, etc.)
    private Context context;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Context {
        private String jobTitle; // Vị trí công việc tuyển dụng (VD: Backend Engineer)
        private String requirement; // Yêu cầu chi tiết về đoạn code hoặc mô tả dự án
        private String prompting; // Prompt bổ sung thêm để hướng dẫn AI sinh đề
    }
}
