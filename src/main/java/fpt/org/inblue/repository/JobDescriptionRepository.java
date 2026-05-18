package fpt.org.inblue.repository;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.enums.JobDescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    @Query("SELECT jd FROM JobDescription jd WHERE jd.isDeleted = false ORDER BY jd.createdAt DESC")
    List<JobDescription> findAllActive();

    @Query("SELECT jd FROM JobDescription jd WHERE jd.status = :status AND jd.isDeleted = false")
    List<JobDescription> findByStatus(@Param("status") JobDescriptionStatus status);
}



