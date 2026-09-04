package fpt.org.inblue.model;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(length = 100, unique = true)
    private String sourceJobId;

    // Phân loại cấp bậc (Intern, Fresher, Junior, Middle, Senior)
    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private TargetLevel level;

    private Double salaryMin;
    private Double salaryMax;

    // Giá gói apply JD (đơn vị: VND). Người dùng phải mua gói này trước khi apply.
    @Column(nullable = true)
    private Long price;

    @OneToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false)
    @JoinColumn(name = "jd_id", nullable = true)
    @OrderBy("roundOrder ASC")
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skillTags;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "skill_embedding", columnDefinition = "vector(384)")
    private float[] skillEmbedding;

    @Builder.Default
    Boolean isDeleted = false;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    LocalDateTime deletedAt;
    LocalDateTime deadlineAt; // Hạn chót nộp hồ sơ

    String companyName;
    String companyLogo;
}
