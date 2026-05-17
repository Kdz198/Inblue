package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer roundOrder;

    @Enumerated(EnumType.STRING)
    private RoundType roundType;

    @Column(name = "pass_threshold")
    private Double passThreshold;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private RoundConfig configData;

    @Builder.Default
    Boolean isDeleted = false;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoundConfig {  // Đống config này cũng để tạm vậy đi chưa cần dùng tới, sẽ sài lúc triển khai factory - pattern method
        // 1. Dành cho Ứng viên đọc
        private String instruction;         // VD: "Bạn hãy viết 1 email xin lỗi khách hàng..."
        private String submissionFormat;    // VD: "TEXT", "PDF", "AUDIO" (FE dựa vào đây để render UI)
        private Integer timeLimitMinutes;   // Giới hạn thời gian làm bài (nếu có)

        // 2. Dành cho AI đọc để chấm điểm
        private String aiSystemPrompt;      // VD: "Bạn là HR Manager đóng vai trò đánh giá ứng viên..."
        private String evaluationCriteria;  // VD: "Cần có lời xin lỗi, điểm trừ nếu đổ lỗi, tối đa 300 chữ..."
    }

    // Tạm thời để vậy trước mốt chỉnh sau
    public enum RoundType {
        CV_SCREENING,          // Vòng lọc CV
        EMAIL_SIMULATOR,       // Vòng giả lập viết Email
        QUIZ,                  // Vòng trắc nghiệm
        AI_BEHAVIORAL_INTERVIEW, // AI phỏng vấn hành vi/văn hóa
        AI_TECH_INTERVIEW      // AI phỏng vấn kỹ thuật
    }
}