package fpt.org.inblue.entrytest.entity;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.entrytest.enums.TargetRole;
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
public class UserCompetency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;
    private Long careerPreferenceId;

    @Enumerated(EnumType.STRING)
    private TargetRole targetRole;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> languagesJson;

    @Enumerated(EnumType.STRING)
    private TargetLevel currentLevel;

    private Double currentScore;
    private Double commonQuizScore;
    private Double specificQuizScore;
    private Double specificCodingScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> competencySnapshotJson;

    private Long lastEntryTestAttemptId;
    private LocalDateTime lastEvaluatedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
