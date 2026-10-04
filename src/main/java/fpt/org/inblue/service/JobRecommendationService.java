package fpt.org.inblue.service;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.JobRecommendationConfig;
import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import fpt.org.inblue.model.dto.response.JobRecommendationThresholdResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JobRecommendationService {
    List<JobRecommendationResponse> getRecommendations(int userId);

    JobRecommendationThresholdResponse updateThreshold(BigDecimal thresholdPercent);
    Optional<JobRecommendationConfig> getThreshold();
}
