package fpt.org.inblue.entrytest.dto.request;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryTestSubmitRequest {
    private List<Answer> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private String itemId;
        private Map<String, Object> answerJson;
    }
}
