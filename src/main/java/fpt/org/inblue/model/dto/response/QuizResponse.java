package fpt.org.inblue.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizResponse {
    int quizId;
    List<QuizItemResponse> items;
}
