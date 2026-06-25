package fpt.org.inblue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReviewProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private CodingProblem.Difficulty difficulty; // EASY, MEDIUM, HARD

    private String language; // Ngôn ngữ lập trình chính của đề bài (VD: "Java", "Javascript", "C#")

    @Column(columnDefinition = "TEXT")
    private String problemStatement; // Ngữ cảnh/Mô tả yêu cầu review

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<CodeFile> files; // Danh sách các file code chứa lỗi cần review

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ExpectedIssue> expectedIssues; // Danh sách lỗi mẫu để AI đối chiếu chấm điểm

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
    public static class CodeFile {
        private String filename; // Ví dụ: "src/main/java/UserService.java"
        private String content; // Nội dung code
        private String language; // Ngôn ngữ lập trình (VD: "java", "sql", "xml") để highlight cú pháp
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpectedIssue {
        private String filename; // Lỗi nằm ở file nào
        private Integer lineNumber; // Dòng bị lỗi (1-indexed)
        private String severity; // Mức độ nghiêm trọng: CRITICAL, WARNING, INFO
        private String description; // Giải thích chi tiết lỗi mẫu và cách fix
    }
}
