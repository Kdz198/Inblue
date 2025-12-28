package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.model.enums.TargetLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class QuestionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int questionSetId;
    String questionSetName;
    String objective;
    TargetLevel level;
    @JoinColumn(name = "major_id")
    @ManyToOne
    Major major;
}
