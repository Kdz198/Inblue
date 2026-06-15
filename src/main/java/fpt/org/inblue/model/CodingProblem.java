package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty; // EASY, MEDIUM, HARD

    @Column(columnDefinition = "TEXT")
    private String problemStatement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> rulesAndConstraints;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> paramTypes;

    private String returnType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Example> visibleExamples; // Input/Output cho ứng viên thấy

    // Cấu hình sandbox
    private Integer executionTimeLimitMs;
    private Integer memoryLimitMb;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> codeStubs; // Key: "JAVA", Value: "class Solution {...}"

    // Test case ẩn để chấm điểm
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<TestCase> hiddenTestCases;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- Nested classes ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Example {
        private List<String> inputs;
        private String output;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestCase {
        private List<String> inputs;
        private String expectedOutput;
        private Integer weightPoints;
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}