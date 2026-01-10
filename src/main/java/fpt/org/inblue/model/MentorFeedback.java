package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MentorFeedback {
    @Id
    int id;
    @MapsId
    @JoinColumn(name = "session_id")
    @OneToOne
    Session session;
    @JoinColumn(name = "mentor_id")
    @ManyToOne
    Mentor mentor;
    @JoinColumn(name = "user_id")
    @ManyToOne
    User user;
    int rating;
    String comment;
}
