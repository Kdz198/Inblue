package fpt.org.inblue.controller;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.service.submission.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application-details")
public class ApplicationDetailController {
    @Autowired
    private SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ApplicationDetail> submitApplicationDetail(@RequestBody SubmitRequest submitRequest) {
        ApplicationDetail result = submissionService.submitRound(submitRequest);
        return ResponseEntity.ok(result);
    }
}
