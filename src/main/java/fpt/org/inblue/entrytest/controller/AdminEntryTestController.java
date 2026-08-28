package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.dto.request.UpsertEntryTestRequest;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.service.AdminEntryTestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/entry-tests")
@RequiredArgsConstructor
public class AdminEntryTestController {
    private final AdminEntryTestService adminEntryTestService;

    @GetMapping
    public ResponseEntity<List<EntryTest>> getAllEntryTests() {
        return ResponseEntity.ok(adminEntryTestService.getAllEntryTests());
    }

    @GetMapping("/active")
    public ResponseEntity<EntryTest> getActiveEntryTest() {
        return ResponseEntity.ok(adminEntryTestService.getActiveEntryTest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntryTest> getEntryTest(@PathVariable Long id) {
        return ResponseEntity.ok(adminEntryTestService.getEntryTest(id));
    }

    @PostMapping
    public ResponseEntity<EntryTest> createEntryTest(@RequestBody UpsertEntryTestRequest request) {
        return ResponseEntity.ok(adminEntryTestService.createEntryTest(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntryTest> updateEntryTest(
            @PathVariable Long id, @RequestBody UpsertEntryTestRequest request) {
        return ResponseEntity.ok(adminEntryTestService.updateEntryTest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntryTest> deactivateEntryTest(@PathVariable Long id) {
        return ResponseEntity.ok(adminEntryTestService.deactivateEntryTest(id));
    }
}
