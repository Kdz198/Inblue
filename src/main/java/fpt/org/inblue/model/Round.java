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

        // --- MỚI: FIELD DÀNH RIÊNG CHO VÒNG CODING (DOCKER SANDBOX) ---
        private List<CodingChallenge> codingChallenges; // Có thể cho thi 1 lúc nhiều bài

        // --- FIELD DÀNH RIÊNG CHO VÒNG CODE_REVIEW ---
        private List<CodeReviewFile> codeReviewFiles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeReviewFile {
        private String fileName;    // VD: "OrderService.java"
        private String language;    // VD: "java" (Để FE highlight syntax cho chuẩn)
        private String fileContent; // Nội dung code chứa lỗ hổng
    }

    // Class con định nghĩa dữ liệu cho Sandbox & UI vòng Coding
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodingChallenge {
        // 1. Phần hiển thị cho ứng viên (Lấy từ DTO của bạn qua)
        private String title;
        private String difficulty;
        private String problemStatement;
        private List<String> rulesAndConstraints;
        private List<Example> visibleExamples; // Input/Output ứng viên nhìn thấy

        // 2. Phần thiết lập cho Docker Sandbox (Quan trọng)
        private Integer executionTimeLimitMs; // VD: 2000 (2 giây)
        private Integer memoryLimitMb;        // VD: 256 (MB)
        private List<String> allowedLanguages;// VD: ["JAVA", "PYTHON", "CPP"]

        // 3. Khung code mẫu cho từng ngôn ngữ (Tùy chọn)
        // Key: "JAVA", Value: "class Solution {\n  public int solve(int[] arr) {\n    return 0;\n  }\n}"
        private Map<String, String> codeStubs;

        // 4. Testcase dùng để chấm điểm (Ẩn với ứng viên)
        private List<TestCase> hiddenTestCases;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Example {
        private String input;
        private String output;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestCase {
        private String input;         // Dữ liệu bơm vào stdin của Docker
        private String expectedOutput;// Dữ liệu hứng từ stdout của Docker để so sánh
        private Integer weightPoints; // Trọng số điểm của testcase này (VD: 10 điểm)
        private Boolean isHidden;     // Mặc định true (Không gửi xuống FE)
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

