package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QuestionLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String categoryName;
    @Column(columnDefinition = "TEXT")
    String description;
    String urlTutorial;
}
