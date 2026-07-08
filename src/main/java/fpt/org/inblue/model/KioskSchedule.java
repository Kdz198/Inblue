package fpt.org.inblue.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kioskId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime openTime;
    private LocalTime closeTime;
    private int slotDurationMinutes;
    private boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
