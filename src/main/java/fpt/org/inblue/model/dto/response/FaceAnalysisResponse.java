package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.enums.BehaviorStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceAnalysisResponse {
    private BehaviorStatus status;
    private boolean isWarning;

}