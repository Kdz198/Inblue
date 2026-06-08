package fpt.org.inblue.repository;

import fpt.org.inblue.model.FeatureUsageLog;
import fpt.org.inblue.enums.FeatureName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureUsageLogRepository extends JpaRepository<FeatureUsageLog, Integer> {
    List<FeatureUsageLog> findAllByFeatureName(FeatureName featureName);
}
