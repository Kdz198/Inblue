package fpt.org.inblue.model;

import fpt.org.inblue.enums.ApplicationStatus;
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
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int userId;
    private Long jdId;

    @Builder.Default
    private Integer currentRoundOrder = 1;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.IN_PROGRESS;

    @Column(name = "overall_score")
    private Double overallScore = -1.0;

    @Builder.Default
    Boolean isDeleted = false;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
