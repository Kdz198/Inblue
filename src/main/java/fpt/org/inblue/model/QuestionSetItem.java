package fpt.org.inblue.model;

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
public class QuestionSetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int questionSetItemId;
    @JoinColumn(name = "question_id")
    @ManyToOne
    Question question;
    @JoinColumn(name = "question_set_id")
    @ManyToOne
    QuestionSet questionSet;
    int orderIndex;
}
