package fpt.org.inblue.service;

import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.dto.response.ApplicationLookupResponse;
import java.util.List;

public interface ApplicationService {
    Application applyForJob(Long jdId);

    Application getApplicationById(Long id);

    List<Application> getAllApplications();

    List<Application> getAllApplicationsByUserId();

    List<ApplicationLookupResponse> getAllApplicationsByUserEmail(String email);

    void moveToNextRound(Application currentApplication);
}
