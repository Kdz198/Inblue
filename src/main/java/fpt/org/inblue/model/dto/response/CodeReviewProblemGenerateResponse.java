package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.CodeReviewProblem.CodeFile;
import fpt.org.inblue.model.CodeReviewProblem.ExpectedIssue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReviewProblemGenerateResponse {
    private String title;
    private String difficulty;
    private String language;
    private String problemStatement;
    private List<CodeFile> files;
    private List<ExpectedIssue> expectedIssues;
}
