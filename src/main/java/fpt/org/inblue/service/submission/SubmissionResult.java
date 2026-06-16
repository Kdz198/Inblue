package fpt.org.inblue.service.submission;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import lombok.Data;

import java.util.List;

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
    private final  List<CompilerResponseDto.TestCaseResult> testCases;
    private SubmissionResult(Status status, Long applicationId, ApplicationDetail detail, String message, ApplicationDetail.RoundResult roundResult, List<CompilerResponseDto.TestCaseResult> testCases) {
        this.status = status;
        this.applicationId = applicationId;
        this.detail = detail;
        this.message = message;
        this.roundResult = roundResult;
        this.testCases = testCases;
    }

    public static SubmissionResult completed(ApplicationDetail detail) {
        return new SubmissionResult(Status.COMPLETED, detail.getApplicationId(), detail, "Nộp bài thành công",detail.getFinalResult(),null);
    }

    public static SubmissionResult pending(Long applicationId) {
        return new SubmissionResult(Status.PENDING, applicationId, null, "Bài đang được chấm, vui lòng chờ",null,null);
    }

    public static SubmissionResult compileCode(CompilerResponseDto compilerResponse) {
        return new SubmissionResult(Status.PENDING, null, null, "Compile code thành công",null,compilerResponse.getTestCases());
    }

}
