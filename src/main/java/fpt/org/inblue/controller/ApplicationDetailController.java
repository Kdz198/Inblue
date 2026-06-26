package fpt.org.inblue.controller;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.CodeReviewSubmitRequest;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import fpt.org.inblue.service.submission.SubmissionResult;
import fpt.org.inblue.service.submission.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application-details")
@RequiredArgsConstructor
public class ApplicationDetailController {
    private final SubmissionService submissionService;
    private final ApplicationDetailService applicationDetailService;
    private final ApplicationService applicationService;
    private final JobDescriptionService jobDescriptionService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Nộp bài ứng tuyển theo từng vòng",
            description =
                    "Hỗ trợ nộp text, danh sách đáp án trắc nghiệm hoặc upload file trực tiếp tùy theo loại vòng thi.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Nộp bài thành công",
                        content = @Content(schema = @Schema(implementation = SubmissionResult.class))),
                @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
            })
    public ResponseEntity<SubmissionResult> submitApplicationDetail(@ModelAttribute SubmitRequest submitRequest)
            throws IOException {
        SubmissionResult result = submissionService.submitRound(submitRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/code-review/evaluate")
    @Operation(
            summary = "Chấm bài Code Review",
            description =
                    "Xử lý chấm điểm trực tiếp bài review code của ứng viên thông qua AnythingLLM với workspace CODE_REVIEW.")
    public ResponseEntity<ApplicationDetail> evaluateCodeReview(@RequestBody CodeReviewSubmitRequest request) {
        ApplicationDetail result = submissionService.evaluateCodeReview(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("hr-score")
    public ResponseEntity<?> hrScore(
            @RequestParam int applicationDetailId,
            @RequestParam boolean isPass,
            @RequestParam String note,
            @RequestParam double score) {
        applicationDetailService.hrScore(applicationDetailId, isPass, note, score);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDetail> getApplicationDetailById(@PathVariable long id) {
        ApplicationDetail applicationDetail = applicationDetailService.getApplicationDetailById(id);
        return ResponseEntity.ok(applicationDetail);
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<?> getApplicationDetailsByApplicationId(@PathVariable long applicationId) {
        return ResponseEntity.ok(applicationDetailService.getByApplicationId(applicationId));
    }

    @GetMapping("/reviewer")
    @Operation(
            summary = "Lấy danh sách các vòng được giao cho reviewer hiện tại (STAFF)",
            description =
                    "Lấy danh sách application details của các vòng thi được gán cho staff hiện tại làm reviewer.")
    public ResponseEntity<?> getApplicationDetailsForReviewer() {
        return ResponseEntity.ok(applicationDetailService.getApplicationDetailsForReviewer());
    }
}
