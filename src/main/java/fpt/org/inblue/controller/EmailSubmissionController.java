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

    // Kích hoạt thủ công logic của scheduleFetchEmails (BackgroundScheduler) để test/debug
    // mà không cần chờ tick @Scheduled(fixedDelay = 60000).
    @PostMapping("/fetch")
    public ResponseEntity<String> triggerFetchEmails() {
        emailSubmissionService.fetchEmails();
        return ResponseEntity.ok("Đã chạy fetchEmails() (quét mail IMAP mới về, tạo các bản ghi PENDING).");
    }

    // Kích hoạt thủ công logic của scheduleProcessPendingEmails (BackgroundScheduler) để test/debug
    // mà không cần chờ tick @Scheduled(fixedDelay = 60000).
    @PostMapping("/process-pending")
    public ResponseEntity<String> triggerProcessPendingEmails() {
        emailSubmissionService.processEmailSchedule();
        return ResponseEntity.ok("Đã chạy processEmailSchedule() (xử lý các email đang PENDING).");
    }
}
