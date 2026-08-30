package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.dto.request.EntryTestRunCodeRequest;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.entrytest.dto.response.EntryTestAttemptResponse;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;
import fpt.org.inblue.entrytest.service.EntryTestService;
import fpt.org.inblue.mapper.EntryTestResponseMapper;
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
    private final EntryTestResponseMapper responseMapper;

    @PostMapping("/start")
    public ResponseEntity<EntryTestStartResponse> startEntryTest() {
        return ResponseEntity.ok(entryTestService.startEntryTest(securityUtils.getCurrentUserId()));
    }

    @PostMapping("/{attemptId}/coding/run")
    public ResponseEntity<CompilerResponseDto> runCode(
            @PathVariable Long attemptId, @Valid @RequestBody EntryTestRunCodeRequest request) {
        return ResponseEntity.ok(entryTestService.runCode(securityUtils.getCurrentUserId(), attemptId, request));
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<EntryTestAttemptResponse> submitEntryTest(
            @PathVariable Long attemptId, @RequestBody EntryTestSubmitRequest request) {
        return ResponseEntity.ok(responseMapper.toAttemptResponse(
                entryTestService.submitEntryTest(securityUtils.getCurrentUserId(), attemptId, request)));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<EntryTestAttemptResponse> getAttemptResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(responseMapper.toAttemptResponse(
                entryTestService.getAttempt(securityUtils.getCurrentUserId(), attemptId)));
    }
}
