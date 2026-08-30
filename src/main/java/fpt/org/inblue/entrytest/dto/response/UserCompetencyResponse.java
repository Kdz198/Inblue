package fpt.org.inblue.entrytest.dto.response;

import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.enums.TargetLevel;
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
public class UserCompetencyResponse {
    private Long id;
    private Integer userId;
    private Integer careerPreferenceId;
    private TargetRole targetRole;
    private List<String> languagesJson;
    private TargetLevel currentLevel;
    private Double currentScore;
    private Double commonQuizScore;
    private Double specificQuizScore;
    private Double specificCodingScore;
    private Map<String, Object> competencySnapshotJson;
    private Long lastEntryTestAttemptId;
    private LocalDateTime lastEvaluatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
