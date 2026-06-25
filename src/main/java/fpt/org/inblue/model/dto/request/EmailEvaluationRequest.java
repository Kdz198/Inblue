package fpt.org.inblue.model.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailEvaluationRequest {
    EmailContext emailContext;
    EvaluationCriteria evaluationCriteria;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailContext {
        private String scenario; // Đề bài / tình huống yêu cầu viết email
        private String level; // Vị trí ứng tuyển (context thêm cho AI)
        private String candidateEmail; // Bài làm của ứng viên
        // FE render dạng email form như To:, Subject:, Body: để ứng viên điền vào, sau đó gửi lên backend dưới dạng
        // text
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationCriteria {
        private Integer maxScore; // thang điểm tối đa để AI chấm điểm trên thang điểm tương ứng
        private String aiSystemPrompt; // CÁC FIELD CHO AI CHẤM ĐIỂM (Dùng cho Tự luận, Email, DB Design, Interview)
        private List<String> extraMetrics;
    }
}
