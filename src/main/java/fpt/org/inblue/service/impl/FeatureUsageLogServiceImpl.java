package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.FeatureUsageLog;
import fpt.org.inblue.model.enums.FeatureName;
import fpt.org.inblue.repository.FeatureUsageLogRepository;
import fpt.org.inblue.service.FeatureUsageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureUsageLogServiceImpl implements FeatureUsageLogService {
    @Autowired
    private FeatureUsageLogRepository repository;
    @Override
    public List<FeatureUsageLog> getAllLogs() {
        return repository.findAll();
    }

    @Override
    public List<FeatureUsageLog> findByFeature(FeatureName feature) {
        return repository.findAllByFeatureName(feature);
    }
}
