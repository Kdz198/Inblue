package fpt.org.inblue.service;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.CreateJobDescriptionRequest;
import fpt.org.inblue.model.dto.request.UpdateJobDescriptionRequest;
import java.io.IOException;
import java.util.List;

public interface JobDescriptionService {

    JobDescription getById(Long id);

    List<JobDescription> getAll();

    List<JobDescription> getByCompanyId(Long companyId);

    JobDescription create(CreateJobDescriptionRequest request) throws IOException;

    JobDescription update(UpdateJobDescriptionRequest request) throws IOException;

    void delete(Long id);

    void softDelete(Long id);

    List<JobDescription> searchJobs(
            String keyword, JobDescriptionStatus status, TargetLevel level, Double salaryMin, Double salaryMax);

    Round getRoundByOrder(Long jdId, int order);

    void toggleActive(Long id);
}
