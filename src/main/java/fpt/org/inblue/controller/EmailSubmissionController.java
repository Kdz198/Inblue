package fpt.org.inblue.controller;

import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.service.submission.EmailSubmissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email-submissions")
@RequiredArgsConstructor
public class EmailSubmissionController {

    private final EmailSubmissionService emailSubmissionService;

    @GetMapping("/{id}")
    public ResponseEntity<EmailSubmission> getById(@PathVariable Long id) {
        return emailSubmissionService
                .getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmailSubmission>> getAll() {
        return ResponseEntity.ok(emailSubmissionService.getAll());
    }
}
