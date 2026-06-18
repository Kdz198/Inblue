package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.CodeReviewProblem.CodeFile;
import fpt.org.inblue.model.CodeReviewProblem.ExpectedIssue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
