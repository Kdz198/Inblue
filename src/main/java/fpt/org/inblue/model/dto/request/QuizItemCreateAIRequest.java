package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Major;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.QuestionLesson;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.model.enums.TargetLevel;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.List;

@Data
public class QuizItemCreateAIRequest {
    String practiceSetName;
    String objective;
    TargetLevel level;
    Major major;
    List<PracticeAIQuestion> questions;

    @Data
    static class PracticeAIQuestion{
        String title;
        String content;
        String answer;
    }
}
