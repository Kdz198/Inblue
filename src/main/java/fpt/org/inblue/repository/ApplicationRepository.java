package fpt.org.inblue.repository;

import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.repository.projection.AdminAnalyticsProjection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByUserId(int userId);

    List<Application> findAllByUserIdAndStatusNot(int userId, ApplicationStatus status);

    List<Application> findByJdIdAndIsDeletedFalse(Long jdId);

    List<Application> findByJdId(Long jdId);

    @Query(
            """
            SELECT jd.id AS jobId, jd.title AS jobTitle, COUNT(a.id) AS applicationCount
            FROM Application a
            JOIN JobDescription jd ON jd.id = a.jdId
            WHERE a.isDeleted = false AND jd.isDeleted = false
            GROUP BY jd.id, jd.title
            ORDER BY COUNT(a.id) DESC
            """)
    List<AdminAnalyticsProjection.JobTrend> findApplicationTrendsByJob(Pageable pageable);

    @Query(
            """
            SELECT a.status AS status, COUNT(a.id) AS applicationCount
            FROM Application a
            WHERE a.isDeleted = false
            GROUP BY a.status
            """)
    List<AdminAnalyticsProjection.ApplicationStatusCount> countApplicationsByStatus();

    @Query(
            """
            SELECT COUNT(a.id) AS totalApplications,
                   COUNT(DISTINCT a.userId) AS uniqueApplicants
            FROM Application a
            WHERE a.isDeleted = false
            """)
    AdminAnalyticsProjection.ApplicationUserStats getApplicationUserStats();
}
