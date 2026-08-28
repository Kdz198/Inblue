package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.dto.request.UpsertCareerPreferenceRequest;
import fpt.org.inblue.entrytest.service.CareerPreferenceService;
import fpt.org.inblue.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/career-preference")
@RequiredArgsConstructor
public class CareerPreferenceController {
    private final CareerPreferenceService careerPreferenceService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<UserCareerPreference> getCurrentPreference() {
        return ResponseEntity.ok(careerPreferenceService.getCurrentPreference(securityUtils.getCurrentUserId()));
    }

     @GetMapping("/exists")
    public ResponseEntity<Boolean> hasCurrentPreference() {
        return ResponseEntity.ok(careerPreferenceService.hasCurrentPreference(securityUtils.getCurrentUserId()));
    }

    @PutMapping
    public ResponseEntity<UserCareerPreference> upsertPreference(
            @RequestBody UpsertCareerPreferenceRequest request) {
        return ResponseEntity.ok(careerPreferenceService.upsertPreference(securityUtils.getCurrentUserId(), request));
    }

    @PostMapping("/skip")
    public ResponseEntity<UserCareerPreference> skipPreference() {
        return ResponseEntity.ok(careerPreferenceService.skipPreference(securityUtils.getCurrentUserId()));
    }
}
