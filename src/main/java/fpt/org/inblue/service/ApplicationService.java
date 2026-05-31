package fpt.org.inblue.service;

import fpt.org.inblue.model.Application;

import java.util.List;

public interface ApplicationService {
    Application applyForJob( Long jdId);
    Application getApplicationById(Long id);
    List<Application> getAllApplications();
    List<Application> getAllApplicationsByUserId();

    void moveToNextRound(Application currentApplication);
}
