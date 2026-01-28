package fpt.org.inblue.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrchestratorConductRequest {

    @JsonProperty("current_anchor")
    private AnchorInfo currentAnchor;

    @JsonProperty("next_anchor")
    private AnchorInfo nextAnchor;

    @JsonProperty("context_history")
    private List<HistoryItem> contextHistory;

    @Data
    @Builder
    public static class AnchorInfo {
        @JsonProperty("question_text")
        private String questionText;

        @JsonProperty("phase_name")
        private String phaseName;
    }

    @Data
    @Builder
    public static class HistoryItem {
        private String role;    // "AI" hoặc "USER"
        private String content;
    }
}