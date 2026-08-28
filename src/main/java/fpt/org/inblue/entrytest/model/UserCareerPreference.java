package fpt.org.inblue.entrytest.model;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.entrytest.enums.TargetRole;
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
public class UserCareerPreference {
    @Id
    private Integer userId;

    @Enumerated(EnumType.STRING)
    private TargetRole targetRole;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> languagesJson;

    private String careerGoal;

    @Enumerated(EnumType.STRING)
    private TargetLevel targetLevel;

    @Builder.Default
    private Boolean needRetest = true;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
