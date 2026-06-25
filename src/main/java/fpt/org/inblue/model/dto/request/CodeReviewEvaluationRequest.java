package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.ApplicationDetail;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeReviewEvaluationRequest {
    EvaluationCriteria evaluationCriteria;
    CodeReviewProblem codeReviewProblem;
    List<ApplicationDetail.CodeReviewSubmission> submissions;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationCriteria {
        private Integer maxScore; // thang điểm tối đa để AI chấm điểm trên thang điểm tương ứng
        private String aiSystemPrompt; // CÁC FIELD CHO AI CHẤM ĐIỂM (Dùng cho Tự luận, Email, DB Design, Interview)
        private List<String> extraMetrics;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CodeReviewProblem {
        private String title;
        private String difficulty;
        private String language;
        private String problemStatement;
        private List<fpt.org.inblue.model.CodeReviewProblem.CodeFile>
                files; // Danh sách các file code chứa lỗi cần review
        private List<fpt.org.inblue.model.CodeReviewProblem.ExpectedIssue>
                expectedIssues; // Danh sách lỗi mẫu để AI đối chiếu chấm điểm
    }
}
