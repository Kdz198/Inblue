package fpt.org.inblue.model;

import fpt.org.inblue.enums.RoundType;
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
    private Boolean isAuto = false;

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
        // Đề bài / tình huống yêu cầu viết email, tự luận, thiết kế DB, phỏng vấn... (VD: "Viết một đoạn văn giải thích về OOP" hoặc "Thiết kế DB cho hệ thống quản lý thư viện")
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


}

