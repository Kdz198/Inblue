package fpt.org.inblue.model.dto.response;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class QuizItemResponse {
    int id;
    private String question;
    private String options;
}
