package fpt.org.inblue.entrytest.dto.request;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.entrytest.enums.TargetRole;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertCareerPreferenceRequest {
    private TargetRole targetRole;
    private List<String> languagesJson;
    private String careerGoal;
    private TargetLevel targetLevel;
}
