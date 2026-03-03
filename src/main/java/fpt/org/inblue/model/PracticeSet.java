package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.TargetLevel;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class PracticeSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String practiceSetName;
    String objective;
    TargetLevel level;
    Date startDate;
    @JoinColumn(name = "major_id")
    @ManyToOne
    Major major;
    @JoinColumn(name = "user_id")
    @ManyToOne
    User user;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<PracticeQuestion> questions;
    Integer interviewSessionId;
}
