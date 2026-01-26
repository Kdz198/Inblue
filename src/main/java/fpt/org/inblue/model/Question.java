package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.QuestionLevel;
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
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int questionId;
    String title;
    @Column(columnDefinition = "TEXT")
    String content;
    QuestionLevel level;
    @JoinColumn(name = "category_id")
    @ManyToOne
    QuestionLesson lesson;
    @Column(columnDefinition = "TEXT")
    String answer;
    //show gợi ý khi người dùng yêu cầu
    String hint;
}
