package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.QuestionLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateQuestionBankRequest {

    @NotNull(message = "Question category ID is required")
    Integer questionCategoryId;

    @NotNull(message = "Question level is required")
    QuestionLevel questionLevel;

    @NotBlank(message = "Question text is required")
    String questionText;

    @NotNull(message = "Options are required")
    List<String> options; // ["A. Spring Boot", "B. Node.js", ...]

    @NotBlank(message = "Correct answer is required")
    String correctAnswer; // "A"
}
