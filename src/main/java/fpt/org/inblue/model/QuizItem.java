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
public class QuizItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(columnDefinition = "TEXT")
    private String question;
    @Column(columnDefinition = "TEXT")
    private String options; // Lưu dạng JSON: {"A": "...", "B": "..."}
    private String correctAnswer;
    private String userResponse;  // Đáp án user chọn
    private String explanation;   // Giải thích của AI (nếu có)
}
