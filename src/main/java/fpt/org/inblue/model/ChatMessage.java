package fpt.org.inblue.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int senderId;
    private String senderType;
    private int recipientId;
    private String recipientType;
    private String content;

    @CreationTimestamp
    private Timestamp timestamp;
}
