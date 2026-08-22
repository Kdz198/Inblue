package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundPlanGenerationResponse {

    private List<RoundDraft> rounds;
    private String globalNotes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoundDraft {
        private String name;
        private Integer roundOrder;
        private RoundType roundType;
        private Double passThreshold;
        private Round.RoundConfig configData;
    }
}
