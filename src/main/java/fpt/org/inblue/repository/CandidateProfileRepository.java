package fpt.org.inblue.repository;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.repository.projection.AdminAnalyticsProjection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Integer> {
    List<CandidateProfile> findByUser_Id(int userId);

    CandidateProfile findByApplicationId(Long applicationId);

    @Query(
            """
            SELECT cp.targetRole AS position, COUNT(cp.id) AS applicationCount
            FROM CandidateProfile cp
            JOIN Application a ON a.id = cp.applicationId
            WHERE a.isDeleted = false
              AND cp.applicationId IS NOT NULL
              AND cp.targetRole IS NOT NULL
              AND TRIM(cp.targetRole) <> ''
            GROUP BY cp.targetRole
            ORDER BY COUNT(cp.id) DESC
            """)
    List<AdminAnalyticsProjection.PositionTrend> findApplicationTrendsByPosition(Pageable pageable);

    CandidateProfile findByUser_IdAndApplicationIdIsNull(int userId);

    void deleteByUser_Id(int userId);
}
