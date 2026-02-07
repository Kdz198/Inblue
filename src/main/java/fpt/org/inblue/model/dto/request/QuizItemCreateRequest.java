package fpt.org.inblue.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizItemCreateRequest {
    String question;
    // Dùng Map để hứng cấu trúc {"A": "...", "B": "..."}
    Map<String, String> options;
    // Đáp án đúng (A, B, C hoặc D)
    String correctAnswer;
    // Giải thích tại sao đúng (AI gen)
    String explanation;
}