package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.QuestionLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class PracticeQuestion {
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
