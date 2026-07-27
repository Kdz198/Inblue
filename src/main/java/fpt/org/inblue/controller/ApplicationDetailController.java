package fpt.org.inblue.controller;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.request.AssignMentorsRequestDto;
import fpt.org.inblue.model.dto.request.CodeReviewSubmitRequest;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.submission.SubmissionResult;
import fpt.org.inblue.service.submission.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application-details")
@RequiredArgsConstructor
public class ApplicationDetailController {
    private final SubmissionService submissionService;
    private final ApplicationDetailService applicationDetailService;

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

    @PutMapping("/{id}/assign-mentor")
    @Operation(
            summary = "Gán 1 mentor cho vòng Mentor Review",
            description = "Dành cho Admin để gán trực tiếp 1 mentor cho vòng thi của ứng viên.")
    public ResponseEntity<ApplicationDetail> assignMentor(@PathVariable long id, @RequestParam int mentorId) {
        ApplicationDetail updated = applicationDetailService.assignMentor(id, mentorId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/assign-mentors")
    @Operation(
            summary = "Gán danh sách nhiều mentor cho vòng Mentor Review",
            description = "Dành cho Admin để đề xuất danh sách nhiều mentor cho ứng viên tự chọn 1 người.")
    public ResponseEntity<ApplicationDetail> assignMentors(
            @PathVariable long id, @RequestBody AssignMentorsRequestDto request) {
        ApplicationDetail updated = applicationDetailService.assignMentors(id, request.getMentorIds());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/assigned-mentors")
    @Operation(
            summary = "Lấy danh sách các mentor được đề xuất cho vòng này",
            description = "Dành cho Ứng viên xem danh sách profile các mentor do Admin đề xuất.")
    public ResponseEntity<List<MentorResponse>> getAssignedMentors(@PathVariable long id) {
        List<MentorResponse> mentors = applicationDetailService.getAssignedMentors(id);
        return ResponseEntity.ok(mentors);
    }

    @PutMapping("/{id}/select-mentor")
    @Operation(
            summary = "Ứng viên chọn 1 mentor cho vòng phỏng vấn",
            description = "Dành cho Ứng viên chọn 1 mentor trong số các mentor được Admin đề xuất.")
    public ResponseEntity<ApplicationDetail> selectMentor(@PathVariable long id, @RequestParam int mentorId) {
        ApplicationDetail updated = applicationDetailService.selectMentor(id, mentorId);
        return ResponseEntity.ok(updated);
    }
}
