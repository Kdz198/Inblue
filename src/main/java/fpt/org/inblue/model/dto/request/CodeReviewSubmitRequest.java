package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.ApplicationDetail.CodeReviewSubmission;
import lombok.Data;
import java.util.List;

@Data
public class CodeReviewSubmitRequest {
    private Long applicationId;
    private Long roundId;
    private List<CodeReviewSubmission> submissions;
}
