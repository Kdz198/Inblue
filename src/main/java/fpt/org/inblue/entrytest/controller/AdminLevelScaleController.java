package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleRequest;
import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleSetRequest;
import fpt.org.inblue.entrytest.entity.LevelScale;
import fpt.org.inblue.entrytest.service.AdminLevelScaleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/level-scales")
@RequiredArgsConstructor
public class AdminLevelScaleController {
    private final AdminLevelScaleService adminLevelScaleService;

    @GetMapping
    public ResponseEntity<List<LevelScale>> getAllLevelScales() {
        return ResponseEntity.ok(adminLevelScaleService.getAllLevelScales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LevelScale> getLevelScale(@PathVariable Long id) {
        return ResponseEntity.ok(adminLevelScaleService.getLevelScale(id));
    }

    @PostMapping
    public ResponseEntity<LevelScale> createLevelScale(@RequestBody UpsertLevelScaleRequest request) {
        return ResponseEntity.ok(adminLevelScaleService.createLevelScale(request));
    }

    @PostMapping("/set")
    public ResponseEntity<List<LevelScale>> upsertLevelScaleSet(@RequestBody UpsertLevelScaleSetRequest request) {
        return ResponseEntity.ok(adminLevelScaleService.upsertLevelScaleSet(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LevelScale> updateLevelScale(
            @PathVariable Long id, @RequestBody UpsertLevelScaleRequest request) {
        return ResponseEntity.ok(adminLevelScaleService.updateLevelScale(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LevelScale> deactivateLevelScale(@PathVariable Long id) {
        return ResponseEntity.ok(adminLevelScaleService.deactivateLevelScale(id));
    }
}
