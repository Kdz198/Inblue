package fpt.org.inblue.model;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round.RoundConfig;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "round_order", nullable = false)
    private Integer roundOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false)
    private RoundType roundType;

    @Column(name = "pass_threshold")
    private Double passThreshold;

    // Tái sử dụng lại class RoundConfig của bảng Round thật
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_data", columnDefinition = "jsonb")
    private RoundConfig configData;
}
