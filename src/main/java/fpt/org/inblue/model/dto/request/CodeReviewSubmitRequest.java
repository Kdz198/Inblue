package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.ApplicationDetail.CodeReviewSubmission;
import java.util.List;
import lombok.Data;

@Data
public class CodeReviewSubmitRequest {
    private Long applicationId;
    private Long roundId;
    private List<CodeReviewSubmission> submissions;
}
