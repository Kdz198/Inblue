package fpt.org.inblue.model.dto.response;

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
public class BehavioralSkillScore {
    private String skillName;
    private Double score;
    private List<String> sourceRounds;
}
