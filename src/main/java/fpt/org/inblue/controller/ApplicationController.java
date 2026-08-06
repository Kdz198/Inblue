package fpt.org.inblue.controller;

import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.model.dto.response.CompetencyChartResponse;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JourneySummaryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final JourneySummaryService journeySummaryService;

    @PostMapping
    public ResponseEntity<Application> applyJd(@RequestParam Long jdId) {
        return ResponseEntity.ok(applicationService.applyForJob(jdId));
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<Application>> getApplicationsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(applicationService.getAllApplicationsByUserEmail(email));
    }

    @GetMapping("/{applicationId}/journey-summary")
    public ResponseEntity<JourneySummary> getJourneySummary(@PathVariable Long applicationId) {
        return ResponseEntity.ok(journeySummaryService.getSavedSummary(applicationId));
    }

    @PostMapping("/{applicationId}/journey-summary/generate")
    public ResponseEntity<JourneySummary> generateJourneySummary(@PathVariable Long applicationId) {
        journeySummaryService.generate(applicationId);
        return ResponseEntity.ok(journeySummaryService.getSavedSummary(applicationId));
    }

    @GetMapping("/{applicationId}/competency-chart")
    public ResponseEntity<CompetencyChartResponse> getCompetencyChart(@PathVariable Long applicationId) {
        return ResponseEntity.ok(journeySummaryService.getSavedCompetencyChart(applicationId));
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
