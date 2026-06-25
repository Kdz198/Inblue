package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.TemplateDto;
import fpt.org.inblue.model.dto.request.UpsertTemplateRequest;
import fpt.org.inblue.service.InterviewTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class InterviewTemplateController {

    private final InterviewTemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateDto.SummaryResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto.DetailResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Mở comment dòng này nếu bạn dùng Spring Security
    public ResponseEntity<Long> createTemplate(@Valid @RequestBody UpsertTemplateRequest request) {
        Long templateId = templateService.createTemplate(request);
        return ResponseEntity.ok(templateId);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTemplate(
            @PathVariable Long id, @Valid @RequestBody UpsertTemplateRequest request) {
        templateService.updateTemplate(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
