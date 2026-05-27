package fpt.org.inblue.model;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;

    private Long roundId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationDetailStatus status = ApplicationDetailStatus.PENDING;

    private Double finalScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private SubmissionData submissionData;

    private Double aiScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private AiFeedback aiFeedback;

    private Double hrScore;

    @Column(columnDefinition = "TEXT")
    private String hrNote;

    @Enumerated(EnumType.STRING)
    private RoundResult finalResult;


    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // --- INNER CLASSES ĐỂ MAP VỚI JSONB ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionData {
        // Dành cho vòng tự luận, Email, SQL Script (Frontend gửi text lên)
        private String textContent;

        // Dành cho vòng upload CV hoặc file kiến trúc (Frontend gửi link file sau khi upload S3)
        private String fileUrl;

        // Dành riêng cho vòng QUIZ
        private List<QuizAnswer> quizAnswers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAnswer {
        private String questionText;
        private String selectedAnswer; // VD: "A"
        private Boolean isCorrect;     // Hệ thống tự check và lưu lại
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiFeedback {
        private String generalComment;      // Nhận xét chung
        private List<String> strengths;     // Điểm mạnh
        private List<String> weaknesses;    // Điểm cần cải thiện
        // Bỏ ngỏ một map để AI trả về các metrics linh hoạt (VD: "độ chuyên nghiệp": 8/10)
        private Map<String, Object> extraMetrics;
    }

    public enum RoundResult {
        PASSED,
        FAILED
    }
}