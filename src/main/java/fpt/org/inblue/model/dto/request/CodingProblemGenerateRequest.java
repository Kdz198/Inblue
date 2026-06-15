package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CodingProblemGenerateRequest {
     String topic;
     String difficulty; // EASY, MEDIUM, HARD
    String targetLevel;
    List<Context> context;
    int numberOfProblems;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    private static class Context{
        String jobTitle;
        String requirement;
        String prompting;
    }
}
