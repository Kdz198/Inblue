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
public class SkillAreaScore {
    private String skillArea;
    private Double score;
    private CompetencyLevel level;
    private List<String> sourceRounds;
}
