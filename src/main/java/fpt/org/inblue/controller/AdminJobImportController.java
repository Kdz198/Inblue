package fpt.org.inblue.controller;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.enums.TopDevJobCategory;
import fpt.org.inblue.model.dto.request.TopDevJobImportRequest;
import fpt.org.inblue.model.dto.response.TopDevJobCategoryResponse;
import fpt.org.inblue.model.dto.response.TopDevJobImportResponse;
import fpt.org.inblue.model.dto.response.TopDevJobPreviewResponse;
import fpt.org.inblue.service.TopDevCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/job-import")
@RequiredArgsConstructor
@Tag(name = "Admin Job Import", description = "Crawl job descriptions for Admin review")
public class AdminJobImportController {

    private final TopDevCrawlerService topDevCrawlerService;

    @GetMapping("/topdev/categories")
    @Operation(summary = "Get supported TopDev job categories")
    public ResponseEntity<List<TopDevJobCategoryResponse>> getTopDevCategories() {
        List<TopDevJobCategoryResponse> categories = Arrays.stream(TopDevJobCategory.values())
                .map(category -> TopDevJobCategoryResponse.builder()
                        .code(category.name())
                        .id(category.getId())
                        .displayName(category.getDisplayName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/topdev/search")
    @Operation(summary = "Search and crawl TopDev job previews")
    public ResponseEntity<List<TopDevJobPreviewResponse>> searchTopDevJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TargetLevel level,
            @RequestParam(required = false, name = "jobCategoriesIds") List<Integer> jobCategoriesIds,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(topDevCrawlerService.searchJobs(keyword, level, jobCategoriesIds, page, limit));
    }

    @PostMapping("/topdev/import")
    @Operation(summary = "Import a selected TopDev JD into Company and JobDescription")
    public ResponseEntity<TopDevJobImportResponse> importTopDevJob(@RequestBody TopDevJobImportRequest request) {
        return ResponseEntity.ok(topDevCrawlerService.importJob(request));
    }
}
