package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionGenerateResponse {
    private String questionText;
    private List<String> options; // ["A. Spring Boot", "B. Node.js", ...]
    private String correctAnswer; // "A"
}
