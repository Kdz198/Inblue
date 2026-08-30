package fpt.org.inblue.entrytest.dto.response;

import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryTestStartResponse {
    private Long attemptId;
    private Long entryTestId;
    private Integer timeLimitMinutes;
    private List<String> selectedLanguagesJson;
    private List<EntryTest.EntryTestSectionConfig> sectionConfigs;
    private List<EntryTestQuestionResponse> commonQuizItemsJson;
    private List<EntryTestQuestionResponse> specificQuizItemsJson;
    private List<EntryTestAttempt.CodingProblemItemSnapshot> specificCodingItemsJson;
}
