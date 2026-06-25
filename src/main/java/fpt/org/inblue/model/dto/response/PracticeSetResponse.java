package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.QuestionLevel;
import fpt.org.inblue.enums.TargetLevel;
import java.sql.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PracticeSetResponse {
    int id;
    String practiceSetName;
    String objective;
    TargetLevel level;
    Date startDate;
    Integer interviewSessionId;
    List<PracticeQuestionDto> questions;
    List<Quiz> quizzes;

    @Data
    @Builder
    public static class PracticeQuestionDto {
        int questionId;
        String title;
        String content;
        QuestionLevel level;
        String lessonName;
        String answer;
        String hint;
    }

    @Data
    @Builder
    public static class Quiz {
        int quizId;
        String quizName;
        int index;
        boolean isSubmit;
    }
}
