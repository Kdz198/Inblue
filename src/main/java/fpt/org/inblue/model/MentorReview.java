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
     int rating;
     String situationNote;
     String taskNote;
     String actionNote;
     String resultNote;
     String strength;
     String weakness;
     String improve;
}
