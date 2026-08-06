package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.CompetencyLevel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyChartResponse {
    private Long applicationId;
    private String candidateName;
    private String jobTitle;
    private CompetencyLevel overallLevel;
    private Double overallScore;
    private List<SkillAreaScore> technicalSkillAreas;
    private List<BehavioralSkillScore> behavioralSkills;
}
