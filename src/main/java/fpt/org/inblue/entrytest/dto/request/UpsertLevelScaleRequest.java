package fpt.org.inblue.entrytest.dto.request;

import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.enums.TargetLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertLevelScaleRequest {
    private TargetRole targetRole;
    private TargetLevel level;
    private Double minScore;
    private Double maxScore;
    private Double minCodingScore;
    private Boolean isActive;
}
