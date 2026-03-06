package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.*;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.model.enums.TargetLevel;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

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
    public static class PracticeQuestionDto{
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
    public static class Quiz{
        int quizId;
        String quizName;
        int index;
        boolean isSubmit;
    }
}
