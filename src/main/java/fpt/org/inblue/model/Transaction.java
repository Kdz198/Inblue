package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction")
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
    @JoinColumn(name ="user_id")
    @ManyToOne
    private User user;
    private boolean type;
    //true là nạp tiền, false là rút tiền
}
