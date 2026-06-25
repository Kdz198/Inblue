package fpt.org.inblue.model;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    // Phân loại cấp bậc (Intern, Fresher, Junior, Middle, Senior)
    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private TargetLevel level;

    private Double salaryMin;
    private Double salaryMax;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "jd_id", nullable = false)
    @Builder.Default
    private List<Round> rounds = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer appliedCount = 0;

    @Column(length = 10)
    @Builder.Default
    private String currency = "VND"; // VND hoặc USD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private JobDescriptionStatus status = JobDescriptionStatus.DRAFT; // OPEN, CLOSED, DRAFT

    @Builder.Default
    Boolean isDeleted = false;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    LocalDateTime deletedAt;
    LocalDateTime deadlineAt; // Hạn chót nộp hồ sơ
}
