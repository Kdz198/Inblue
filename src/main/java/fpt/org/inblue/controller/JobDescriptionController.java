package fpt.org.inblue.controller;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.CreateJobDescriptionRequest;
import fpt.org.inblue.model.dto.request.UpdateJobDescriptionRequest;
import fpt.org.inblue.model.enums.JobDescriptionStatus;
import fpt.org.inblue.model.enums.TargetLevel;
import fpt.org.inblue.service.JobDescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-descriptions")
@Tag(name = "Job Description", description = "API for Job Description Management")
public class JobDescriptionController {

    @Autowired
    private JobDescriptionService jobDescriptionService;

    @GetMapping
    @Operation(summary = "Get all job descriptions")
    public ResponseEntity<List<JobDescription>> getAll() {
        List<JobDescription> jobDescriptions = jobDescriptionService.getAll();
        return ResponseEntity.ok(jobDescriptions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job description by ID")
    public ResponseEntity<JobDescription> getById(@PathVariable Long id) {
        JobDescription jobDescription = jobDescriptionService.getById(id);
        return ResponseEntity.ok(jobDescription);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get all job descriptions by company ID")
    public ResponseEntity<List<JobDescription>> getByCompanyId(@PathVariable Long companyId) {
        List<JobDescription> jobDescriptions = jobDescriptionService.getByCompanyId(companyId);
        return ResponseEntity.ok(jobDescriptions);
    }

    @PostMapping
    @Operation(summary = "Create a new job description")
    public ResponseEntity<JobDescription> create(@RequestBody CreateJobDescriptionRequest request) throws IOException {
        JobDescription createdJobDescription = jobDescriptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJobDescription);
    }

    @PutMapping
    @Operation(summary = "Update a job description")
    public ResponseEntity<JobDescription> update(@RequestBody UpdateJobDescriptionRequest request) throws IOException {
        JobDescription updatedJobDescription = jobDescriptionService.update(request);
        return ResponseEntity.ok(updatedJobDescription);
    }

//    @DeleteMapping("/{id}")
//    @Operation(summary = "Permanent delete a job description")
//    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
//        jobDescriptionService.delete(id);
//        return ResponseEntity.ok(Map.of("message", "Xóa mô tả công việc thành công"));
//    }

    @DeleteMapping("/{id}/soft")
    @Operation(summary = "Soft delete a job description")
    public ResponseEntity<Map<String, String>> softDelete(@PathVariable Long id) {
        jobDescriptionService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "Xóa mô tả công việc thành công"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search job descriptions by keyword and status")
    public ResponseEntity<List<JobDescription>> searchJobs(
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) JobDescriptionStatus status,
            @RequestParam(required = false) TargetLevel level,
            @RequestParam(required = false) Double salaryMin,
            @RequestParam(required = false) Double salaryMax) {
        return ResponseEntity.ok(jobDescriptionService.searchJobs(titleKeyword, status,level,salaryMin,salaryMax));
    }
}

