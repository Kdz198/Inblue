package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

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
    public static class RoundConfig {
        // --- CÁC FIELD DÙNG CHUNG CHO MỌI VÒNG ---
        private String instruction;
        private String submissionFormat;    // TEXT, PDF, MULTIPLE_CHOICE, SQL_SCRIPT, MERMAID
        private Integer timeLimitMinutes;
        private Integer maxScore;           // Thang điểm tối đa (VD: 100)

        // --- CÁC FIELD CHO AI CHẤM ĐIỂM (Dùng cho Tự luận, Email, DB Design, Interview) ---
        private String aiSystemPrompt;
        private String evaluationCriteria;

        // --- FIELD DÀNH RIÊNG CHO VÒNG QUIZ ---
        // Nếu không phải vòng QUIZ, list này cứ để null, PostgreSQL jsonb sẽ tự động bỏ qua không lưu
        private List<QuizQuestion> quizQuestions;
    }

    // Class con định nghĩa cấu trúc 1 câu hỏi trắc nghiệm
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizQuestion {
        private String questionText;    // Nội dung câu hỏi
        private List<String> options;   // Danh sách đáp án: ["A. Spring Boot", "B. Nodejs", "C. Django"]
        private String correctAnswer;   // Đáp án đúng (VD: "A")
        private Integer points;         // Điểm của câu này (VD: 10)
    }

    public enum RoundType {
        CV_SCREENING,          // Vòng lọc CV
        EMAIL_SIMULATOR,       // Vòng giả lập viết Email
        QUIZ,                  // Vòng trắc nghiệm
        DB_DESIGN,             // Vòng thiết kế Cơ sở dữ liệu
        AI_INTERVIEW           // Gộp chung Behavioral & Tech cho gọn
    }
}