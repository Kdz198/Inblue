package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private long amount;
    private String description;
    private String transactionCode;
    @JoinColumn(name ="user_id")
    @ManyToOne
    private User user;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private boolean transactionType;
    private long currentBalance;
    //true là nạp tiền, false là rút tiền
}
