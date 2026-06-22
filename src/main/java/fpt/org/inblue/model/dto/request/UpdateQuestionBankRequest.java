package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.QuestionLevel;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateQuestionBankRequest {

    Integer questionCategoryId;

    QuestionLevel questionLevel;

    @Size(min = 1, message = "Question text must not be empty")
    String questionText;

    List<String> options; // ["A. Spring Boot", "B. Node.js", ...]

    @Size(min = 1, message = "Correct answer must not be empty")
    String correctAnswer; // "A"
}
