package fpt.org.inblue.repository;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.JobDescription;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Long>, JpaSpecificationExecutor<JobDescription> {

    List<JobDescription> findByStatusAndDeadlineAtBefore(JobDescriptionStatus status, LocalDateTime now);

    List<JobDescription> findByStatusAndIsDeletedFalse(JobDescriptionStatus status);

    List<JobDescription> findByIsDeletedFalse();
}
