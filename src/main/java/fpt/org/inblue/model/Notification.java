package fpt.org.inblue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Notification {
    @Id
    private int id;
    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;
    private String title;
    private String message;
    private boolean isRead;
    @CreationTimestamp
    private LocalDateTime createAt;
}
