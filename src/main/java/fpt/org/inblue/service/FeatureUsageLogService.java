package fpt.org.inblue.service;

import fpt.org.inblue.model.FeatureUsageLog;
import fpt.org.inblue.enums.FeatureName;

import java.util.List;

public interface FeatureUsageLogService {
    List<FeatureUsageLog> getAllLogs();
    List<FeatureUsageLog> findByFeature(FeatureName feature);
}
