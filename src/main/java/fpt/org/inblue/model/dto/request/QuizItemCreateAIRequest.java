package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.TargetLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizItemCreateAIRequest {
    String practiceSetName;
    String objective;
    TargetLevel level;
    String majorName;
    List<PracticeAIQuestion> questions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PracticeAIQuestion{
        String title;
        String content;
        String answer;
    }
}
