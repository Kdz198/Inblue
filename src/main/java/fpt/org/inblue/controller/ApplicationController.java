package fpt.org.inblue.controller;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")

@RequiredArgsConstructor

public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Application> applyJd(@RequestParam Long jdId) {
        return ResponseEntity.ok(applicationService.applyForJob(jdId));
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Application>> getAllApplicationsByUserId() {
        return ResponseEntity.ok(applicationService.getAllApplicationsByUserId());
    }

}
