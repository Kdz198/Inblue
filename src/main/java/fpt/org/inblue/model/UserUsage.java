package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class UserUsage {
    @Id
    int userId;
    @MapsId
    @JoinColumn(name = "user_id")
    @OneToOne
    User user;
    int aiInterviewUsed;
    int practiceSetUsed;
    int quizSetUsed;
    LocalDate expiredAt;

}
