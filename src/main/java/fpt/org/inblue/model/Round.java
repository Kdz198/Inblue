package fpt.org.inblue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fpt.org.inblue.enums.RoundType;
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
        // --- CÁC FIELD DÙNG CHUNG ---
        private String instruction;
        private String submissionFormat;
        private Integer timeLimitMinutes;
        private Integer maxScore;

        // --- CÁC FIELD CHO AI CHẤM ĐIỂM ---
        private String aiSystemPrompt;
        // Đề bài / tình huống yêu cầu viết email, tự luận, thiết kế DB, phỏng vấn... (VD: "Viết một đoạn văn giải thích về OOP" hoặc "Thiết kế DB cho hệ thống quản lý thư viện")
        private String evaluationCriteria;

        // --- FIELD CHO VÒNG QUIZ ---
        private List<QuizQuestion> quizQuestions;
        private List<CodingProblemSnapshot> codingProblems; // Dùng để lưu snapshot của bài coding khi tạo round, tránh bị ảnh hưởng nếu bài gốc bị sửa sau đó

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodingProblemSnapshot {
        private Long problemId;          // Giữ lại để trace về bài gốc nếu cần
        private String title;
        private CodingProblem.Difficulty difficulty;
        private String problemStatement;
        private List<String> rulesAndConstraints;
        private List<CodingProblem.Example> visibleExamples;
        private Integer executionTimeLimitMs;
        private Integer memoryLimitMb;
        private Map<String, String> codeStubs;
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

