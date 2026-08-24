package fpt.org.inblue.entrytest.entity;

import fpt.org.inblue.model.CodingProblem;
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
public class EntryTestAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;
    private Long careerPreferenceId;
    private Long entryTestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> selectedLanguagesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<QuestionItemSnapshot> commonQuizItemsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<QuestionItemSnapshot> specificQuizItemsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<CodingProblemItemSnapshot> specificCodingItemsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<EntryTestAnswerSnapshot> answersJson;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Double commonQuizScore;
    private Double specificQuizScore;
    private Double specificCodingScore;
    private Double finalScore;
    private String resultLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> resultSnapshotJson;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AttemptStatus {
        IN_PROGRESS,
        SUBMITTED,
        GRADED,
        EXPIRED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionItemSnapshot {
        private String itemId;
        private Integer questionBankId;
        private String questionText;
        private List<String> options;
        private String correctAnswer;
        private String categoryName;
        private String difficulty;
        private Double maxScore;
        private Integer displayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodingProblemItemSnapshot {
        private String itemId;
        private Long codingProblemId;
        private String title;
        private String difficulty;
        private String problemStatement;
        private List<String> rulesAndConstraints;
        private List<CodingProblem.Example> visibleExamples;
        private Map<String, String> codeStubs;
        private List<String> paramTypes;
        private String returnType;
        private Integer executionTimeLimitMs;
        private Integer memoryLimitMb;
        private Double maxScore;
        private Integer displayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntryTestAnswerSnapshot {
        private String itemId;
        private EntryTest.SectionType sectionType;
        private EntryTest.ItemType answerType;
        private Map<String, Object> answerJson;
        private Double score;
        private Boolean isCorrect;
        private LocalDateTime gradedAt;
    }
}
