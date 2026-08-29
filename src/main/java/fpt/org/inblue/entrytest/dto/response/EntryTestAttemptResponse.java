package fpt.org.inblue.entrytest.dto.response;

import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import java.time.LocalDateTime;
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
public class EntryTestAttemptResponse {
    private Long id;
    private Integer userId;
    private Integer careerPreferenceId;
    private Long entryTestId;
    private List<String> selectedLanguagesJson;
    private List<EntryTestQuestionResponse> commonQuizItemsJson;
    private List<EntryTestQuestionResponse> specificQuizItemsJson;
    private List<EntryTestAttempt.CodingProblemItemSnapshot> specificCodingItemsJson;
    private List<EntryTestAttempt.EntryTestAnswerSnapshot> answersJson;
    private EntryTestAttempt.AttemptStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Double commonQuizScore;
    private Double specificQuizScore;
    private Double specificCodingScore;
    private Double finalScore;
    private String resultLevel;
    private Map<String, Object> resultSnapshotJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
