package fpt.org.inblue.model.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CvEvaluationRequest {
    @JsonIgnore
    MultipartFile cvFile;
    EvaluationCriteria evaluationCriteria;
    JD jobDescription;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationCriteria{
        private Integer maxScore; //thang điểm tối đa để AI chấm điểm trên thang điểm tương ứng
        private String aiSystemPrompt;//CÁC FIELD CHO AI CHẤM ĐIỂM (Dùng cho Tự luận, Email, DB Design, Interview)
        private List<String> extraMetrics;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JD{
        private String title;
        private String description;
        private String requirements;
        private String level;
    }

}
