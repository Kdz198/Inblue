package fpt.org.inblue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kiosk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
