package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import fpt.org.inblue.model.dto.response.JobRecommendationThresholdResponse;
import java.math.BigDecimal;
import java.util.List;

public interface JobRecommendationService {
    List<JobRecommendationResponse> getRecommendations(int userId);

    JobRecommendationThresholdResponse updateThreshold(BigDecimal thresholdPercent);
}
