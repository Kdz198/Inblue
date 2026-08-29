package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.dto.request.EntryTestRunCodeRequest;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;
import fpt.org.inblue.entrytest.service.EntryTestService;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entry-tests")
@RequiredArgsConstructor
public class EntryTestController {
    private final EntryTestService entryTestService;
    private final SecurityUtils securityUtils;

    @PostMapping("/start")
    public ResponseEntity<EntryTestStartResponse> startEntryTest() {
        return ResponseEntity.ok(entryTestService.startEntryTest(securityUtils.getCurrentUserId()));
    }

    @PostMapping("/{attemptId}/coding/run")
    public ResponseEntity<CompilerResponseDto> runCode(
            @PathVariable Long attemptId, @Valid @RequestBody EntryTestRunCodeRequest request) {
        return ResponseEntity.ok(entryTestService.runCode(
                securityUtils.getCurrentUserId(), attemptId, request));
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<EntryTestAttempt> submitEntryTest(
            @PathVariable Long attemptId, @RequestBody EntryTestSubmitRequest request) {
        return ResponseEntity.ok(entryTestService.submitEntryTest(
                securityUtils.getCurrentUserId(), attemptId, request));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<EntryTestAttempt> getAttemptResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(entryTestService.getAttempt(securityUtils.getCurrentUserId(), attemptId));
    }
}
