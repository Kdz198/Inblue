package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MentorReview {
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

    @Column(columnDefinition = "TEXT")
    String situationNote;

    @Column(columnDefinition = "TEXT")
    String taskNote;

    @Column(columnDefinition = "TEXT")
    String actionNote;

    @Column(columnDefinition = "TEXT")
    String resultNote;

    @Column(columnDefinition = "TEXT")
    String strength;

    @Column(columnDefinition = "TEXT")
    String weakness;

    @Column(columnDefinition = "TEXT")
    String improve;
}
