package fpt.org.inblue.controller;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.SubmissionResult;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.submission.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/application-details")
public class ApplicationDetailController {
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private ApplicationDetailService applicationDetailService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Nộp bài ứng tuyển theo từng vòng",
            description = "Hỗ trợ nộp text, danh sách đáp án trắc nghiệm hoặc upload file trực tiếp tùy theo loại vòng thi.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Nộp bài thành công",
                            content = @Content(schema = @Schema(implementation = SubmissionResult.class))),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
            }
    )
    public ResponseEntity<SubmissionResult> submitApplicationDetail(@ModelAttribute SubmitRequest submitRequest) throws IOException {
        System.out.println("Received submit request: " + submitRequest);
        SubmissionResult result = submissionService.submitRound(submitRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("hr-score")
    public ResponseEntity<?> hrScore(@RequestParam int applicationId,
                             @RequestParam boolean isPass,
                             @RequestParam String note,
                             @RequestParam double score){
        applicationDetailService.hrScore(applicationId, isPass, note, score);
        return ResponseEntity.ok().build();
     }

     @GetMapping("/{id}")
        public ResponseEntity<ApplicationDetail> getApplicationDetailById(@PathVariable long id){
            ApplicationDetail applicationDetail = applicationDetailService.getApplicationById(id);
            return ResponseEntity.ok(applicationDetail);
     }

     @GetMapping("/application/{applicationId}")
        public ResponseEntity<?> getApplicationDetailsByApplicationId(@PathVariable long applicationId){
            return ResponseEntity.ok(applicationDetailService.getByApplicationId(applicationId));
     }
}
