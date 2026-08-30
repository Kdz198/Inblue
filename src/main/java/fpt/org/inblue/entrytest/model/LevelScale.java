package fpt.org.inblue.entrytest.model;

import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.enums.TargetLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelScale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TargetRole targetRole;

    @Enumerated(EnumType.STRING)
    private TargetLevel level;

    private Double minScore;
    private Double maxScore;
    private Double minCodingScore;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
