package fpt.org.inblue.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResultDetail {

    // Nhận xét tổng quan của AI về ứng viên (Vd: "Ứng viên có kiến thức tốt về Spring nhưng yếu SQL...")
    private String aiOverviewFeedback;

    // Gợi ý lộ trình học tập tiếp theo
    private String improvementPlan;

    // Chi tiết từng câu hỏi (Map từ Redis sang)
    private List<QAResult> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QAResult {
        private int questionOrder;
        private String questionText;
        private String answerText;

        // Phần này sẽ có sau khi AI chấm điểm xong
        private String feedback; // AI nhận xét câu này
        private Double score;    // Điểm câu này (0-10)
        private String suggestion; // Gợi ý câu trả lời tốt hơn
    }
}