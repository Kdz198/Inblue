package fpt.org.inblue.model;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.MeetingType;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

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

    @CreationTimestamp
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "mentor_review_id")
    MentorReview mentorReview; // Nếu vòng này có phần đánh giá của mentor, sẽ liên kết đến bảng MentorReview

    private Long sessionId;
    private Long bookingId;

    private Integer mentorId; // ID mentor được Admin gán vào vòng này

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private RoundSessionInfo sessionInfo; // Lưu thông tin session (sessionId, meetingType, startTime, endTime)

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // --- INNER CLASSES ĐỂ MAP VỚI JSONB ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoundSessionInfo {
        private Integer sessionId;
        private MeetingType meetingType; // ONLINE hoặc OFFLINE
        private java.sql.Timestamp startTime; // Thời gian bắt đầu hẹn gặp (ứng viên + mentor tự chọn)
        private java.sql.Timestamp endTime;   // Thời gian kết thúc hẹn gặp
    }

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
        List<CodeSubmission>
                codeSubmissions; // Dành riêng cho vòng Coding (Frontend gửi lên cấu trúc JSON gồm source code + kết quả
        // test case)
        private List<CodeReviewSubmission> codeReviewSubmissions; // Dành riêng cho vòng Code Review để lưu lại bài làm
        private Long emailSubmissionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeSubmission {
        private List<String> sourceCode;
        private CompilerResponseDto testCases;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeReviewSubmission {
        private String filename; // Lỗi nằm ở file nào
        private Integer lineNumber; // Dòng bị lỗi (1-indexed)
        private String severity; // Mức độ nghiêm trọng: CRITICAL, WARNING, INFO
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAnswer {
        private String questionText;
        private String selectedAnswer; // VD: "A"
        private Boolean isCorrect; // Hệ thống tự check và lưu lại
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiFeedback {
        private String generalComment; // Nhận xét chung
        private List<String> strengths; // Điểm mạnh
        private List<String> weaknesses; // Điểm cần cải thiện
        // Bỏ ngỏ một map để AI trả về các metrics linh hoạt (VD: "độ chuyên nghiệp": 8/10)
        private Map<String, Object> extraMetrics;
    }

    public enum RoundResult {
        PASSED,
        FAILED
    }
}
