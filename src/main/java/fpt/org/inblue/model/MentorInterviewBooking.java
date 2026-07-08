package fpt.org.inblue.model;

import fpt.org.inblue.enums.BookingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorInterviewBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationDetailId;
    private Long kioskId;
    private int applicantUserId;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private Integer mentorId;
    private Integer sessionId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String sessionKey;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
