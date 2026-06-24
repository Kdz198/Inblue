package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.ApplicationDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeReviewEvaluationRequest {
    EvaluationCriteria evaluationCriteria;
    CodeReviewProblem codeReviewProblem;
    ApplicationDetail.CodeReviewSubmission submission;

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
    public static class CodeReviewProblem{
        private String title;
        private String difficulty;
        private String language;
        private String problemStatement;
        private List<fpt.org.inblue.model.CodeReviewProblem.CodeFile> files; // Danh sách các file code chứa lỗi cần review
        private List<fpt.org.inblue.model.CodeReviewProblem.ExpectedIssue> expectedIssues; // Danh sách lỗi mẫu để AI đối chiếu chấm điểm
    }
}
