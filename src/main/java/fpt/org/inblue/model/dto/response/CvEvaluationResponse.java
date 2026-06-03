package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CvEvaluationResponse {
    private Double score; // Điểm số cuối cùng sau khi AI chấm điểm
    private Map<String, Object> extraMetrics;

}
