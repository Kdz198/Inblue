package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.ApplicationDetail;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CvEvaluationResponse {
    private Double score; // Điểm số cuối cùng sau khi AI chấm điểm
    private Map<String, Object> extraMetrics;
    private ApplicationDetail.StructuredAiFeedback structuredAiFeedback;
}
