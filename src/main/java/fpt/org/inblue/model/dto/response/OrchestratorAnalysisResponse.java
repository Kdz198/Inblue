package fpt.org.inblue.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorAnalysisResponse {

    @JsonProperty("action")
    private AnalysisAction action;

    // AI trả về nội dung câu hỏi tiếp theo (Bồi hoặc Humanized Blueprint)
    @JsonProperty("response_text")
    private String responseText;

    public enum AnalysisAction {
        DRILL_DOWN, // Hỏi bồi
        MOVE_NEXT,  // Qua câu mới
        CLARIFY_AND_SUPPORT, // Khi ứng viên hỏi ngược lại AI
        FINISH      // (Optional) Cho nghỉ sớm
    }
}