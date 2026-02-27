package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    long amount;
    String description;
    @JoinColumn(name ="user_id")
    @ManyToOne
    private User user;
    PaymentStatus status ;
    @CreationTimestamp
    LocalDateTime createdAt;
    LocalDateTime payAt;
}
