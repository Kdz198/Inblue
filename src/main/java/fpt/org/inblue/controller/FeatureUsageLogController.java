package fpt.org.inblue.controller;

import fpt.org.inblue.model.FeatureUsageLog;
import fpt.org.inblue.model.enums.FeatureName;
import fpt.org.inblue.service.FeatureUsageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feature-usage-logs")
public class FeatureUsageLogController {
    @Autowired
    private FeatureUsageLogService featureUsageLogService;

    @GetMapping
    public ResponseEntity<List<FeatureUsageLog>> getAll(){
        return ResponseEntity.ok(featureUsageLogService.getAllLogs());
    }

    @GetMapping("/by-feature")
    public ResponseEntity<List<FeatureUsageLog>> getByFeature(@RequestParam FeatureName featureName) {
        return ResponseEntity.ok(featureUsageLogService.findByFeature(featureName));
    }
}
