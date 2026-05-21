package fpt.org.inblue.controller;

import fpt.org.inblue.model.Application;
import fpt.org.inblue.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

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
