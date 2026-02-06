package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class PracticeSetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @JoinColumn(name = "question_id")
    @ManyToOne
    PracticeQuestion practiceQuestion;
    @JoinColumn(name = "practice_set_id")
    @ManyToOne
    PracticeSet practiceSet;
    int orderIndex;
}
