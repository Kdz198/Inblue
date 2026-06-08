package fpt.org.inblue.service;

import fpt.org.inblue.model.ApplicationDetail;

import java.util.List;

public interface ApplicationDetailService {
    ApplicationDetail getApplicationById(long id);
    List<ApplicationDetail> getByApplicationId(long applicationId);
    void hrScore(int applicationId,boolean isPass, String note);
}
