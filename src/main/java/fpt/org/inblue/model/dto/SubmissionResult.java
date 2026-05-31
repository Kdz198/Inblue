package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.ApplicationDetail;
import lombok.Data;

@Data
public class SubmissionResult {

    public enum Status{
        PENDING,
        COMPLETED
    }

    private final Status status;
    private final Long applicationId;
    private final ApplicationDetail detail;   // null nếu PENDING
    private final String message;
    private final ApplicationDetail.RoundResult roundResult;
    private SubmissionResult(Status status, Long applicationId, ApplicationDetail detail, String message, ApplicationDetail.RoundResult roundResult) {
        this.status = status;
        this.applicationId = applicationId;
        this.detail = detail;
        this.message = message;
        this.roundResult = roundResult;
    }

    public static SubmissionResult completed(ApplicationDetail detail) {
        return new SubmissionResult(Status.COMPLETED, detail.getApplicationId(), detail, "Nộp bài thành công",detail.getFinalResult());
    }

    public static SubmissionResult pending(Long applicationId) {
        return new SubmissionResult(Status.PENDING, applicationId, null, "Bài đang được chấm, vui lòng chờ",null);
    }

}
