package fpt.org.inblue.entrytest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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
public class EntryTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private Double totalScore = 100.0;

    @Builder.Default
    private Integer timeLimitMinutes = 60;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<EntryTestSectionConfig> sectionConfigs = defaultSectionConfigs();

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private static List<EntryTestSectionConfig> defaultSectionConfigs() {
        return List.of(
                EntryTestSectionConfig.builder()
                        .sectionType(SectionType.COMMON_QUIZ)
                        .itemType(ItemType.QUESTION_BANK)
                        .itemCount(15)
                        .totalScore(30.0)
                        .scorePerItem(2.0)
                        .displayOrder(1)
                        .build(),
                EntryTestSectionConfig.builder()
                        .sectionType(SectionType.SPECIFIC_QUIZ)
                        .itemType(ItemType.QUESTION_BANK)
                        .itemCount(12)
                        .totalScore(30.0)
                        .scorePerItem(2.5)
                        .displayOrder(2)
                        .build(),
                EntryTestSectionConfig.builder()
                        .sectionType(SectionType.SPECIFIC_CODING)
                        .itemType(ItemType.CODING_PROBLEM)
                        .itemCount(1)
                        .totalScore(40.0)
                        .scorePerItem(40.0)
                        .displayOrder(3)
                        .build());
    }

    public enum SectionType {
        COMMON_QUIZ,
        SPECIFIC_QUIZ,
        SPECIFIC_CODING
    }

    public enum ItemType {
        QUESTION_BANK,
        CODING_PROBLEM
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntryTestSectionConfig {
        private SectionType sectionType;
        private ItemType itemType;
        private Integer itemCount;
        private Double totalScore;
        private Double scorePerItem;
        private Integer displayOrder;
    }
}
