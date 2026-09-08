package fpt.org.inblue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRecommendationConfig {
    public static final Integer SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal matchThresholdPercent;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
