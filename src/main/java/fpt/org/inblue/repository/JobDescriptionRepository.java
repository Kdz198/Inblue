package fpt.org.inblue.repository;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.JobDescription;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Long>, JpaSpecificationExecutor<JobDescription> {

    List<JobDescription> findByStatusAndDeadlineAtBefore(JobDescriptionStatus status, LocalDateTime now);

    List<JobDescription> findByStatusAndIsDeletedFalse(JobDescriptionStatus status);

    List<JobDescription> findByIsDeletedFalse();

    Optional<JobDescription> findFirstBySourceJobIdAndIsDeletedFalse(String sourceJobId);

    @Query(
            value = """
            SELECT *
            FROM jobdescription
            WHERE isdeleted = false
              AND status != 'CLOSED'
              AND skill_embedding IS NOT NULL
            ORDER BY skill_embedding <=> cast(:vectorStr as vector) ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<JobDescription> findTopRecommendedJobs(
            @Param("vectorStr") String vectorStr,
            @Param("limit") int limit);
}
