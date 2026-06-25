package fpt.org.inblue.service;

import fpt.org.inblue.model.ApplicationDetail;
import java.util.List;

public interface ApplicationDetailService {
    ApplicationDetail getApplicationDetailById(long id);

    List<ApplicationDetail> getByApplicationId(long applicationId);

    void hrScore(long applicationId, boolean isPass, String note, double score);

    List<ApplicationDetail> getApplicationDetailsForReviewer();
}
