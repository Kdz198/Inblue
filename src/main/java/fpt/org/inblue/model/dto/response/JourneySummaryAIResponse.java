package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.CompetencyLevel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneySummaryAIResponse {
    private String narrative;
    private CompetencyChartResponse competencyChart;
    private List<SwecomSkillAssessment> swecomAssessments;
    private List<DevelopmentRecommendation> developmentRecommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SwecomSkillAssessment {
        private String skillArea;
        private CompetencyLevel level;
        private Double score;
        private String evidenceSummary;
        private List<String> sourceRounds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevelopmentRecommendation {
        private String targetSkillArea;
        private String recommendation;
        private CompetencyLevel targetLevel;
    }
}
