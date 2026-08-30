package fpt.org.inblue.entrytest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.model.User;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_competency_user"))
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    private Integer careerPreferenceId;

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
