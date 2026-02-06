package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.enums.QuestionLevel;
import lombok.Data;

@Data
public class PracticeQuestionRequest {
    String title;
    String content;
    QuestionLevel level;
    String lessonName;
    String answer;
    String hint;
}
