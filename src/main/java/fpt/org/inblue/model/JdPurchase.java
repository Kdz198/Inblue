package fpt.org.inblue.model;

import fpt.org.inblue.enums.JdPurchaseStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "jd_purchase")
public class JdPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int userId;

    @Column(nullable = false)
    private Long jdId;

    private int paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private JdPurchaseStatus status = JdPurchaseStatus.PURCHASED;

    @CreationTimestamp
    private LocalDateTime purchasedAt;

    private LocalDateTime usedAt;
}
