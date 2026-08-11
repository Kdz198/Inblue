package fpt.org.inblue.repository;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.response.admin.ApplicationDetailBasicProjection;
import fpt.org.inblue.repository.projection.AdminAnalyticsProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationDetailRepository extends JpaRepository<ApplicationDetail, Long> {
    ApplicationDetail findByApplicationId(Long applicationId);

    List<ApplicationDetail> findAllByApplicationId(Long applicationId);

    Optional<ApplicationDetail> findByApplicationIdAndRoundId(Long applicationId, Long roundId);

    Optional<ApplicationDetail> findBySessionId(Integer sessionId);

    List<ApplicationDetail> findAllByStatus(ApplicationDetailStatus status);

    @Query(
            """
            SELECT ad.id AS applicationDetailId,
                   ad.applicationId AS applicationId,
                   a.userId AS userId,
                   u.name AS userName,
                   u.email AS userEmail,
                   a.jdId AS jobId,
                   jd.title AS jobTitle,
                   r.id AS roundId,
                   r.roundOrder AS roundOrder,
                   r.name AS roundName,
                   r.roundType AS roundType,
                   ad.status AS roundStatus,
                   ad.startedAt AS startedAt,
                   ad.updatedAt AS updatedAt
            FROM ApplicationDetail ad
            JOIN Application a ON a.id = ad.applicationId
            JOIN Round r ON r.id = ad.roundId
            LEFT JOIN User u ON u.id = a.userId
            LEFT JOIN JobDescription jd ON jd.id = a.jdId
            WHERE a.isDeleted = false
              AND (r.isDeleted = false OR r.isDeleted IS NULL)
              AND a.status = fpt.org.inblue.enums.ApplicationStatus.IN_PROGRESS
              AND ad.status IN (:activeStatuses)
              AND r.roundType IN (:interviewRoundTypes)
            ORDER BY COALESCE(ad.updatedAt, ad.createdAt) DESC
            """)
    List<AdminAnalyticsProjection.ActiveInterview> findActiveInterviews(
            @Param("activeStatuses") List<ApplicationDetailStatus> activeStatuses,
            @Param("interviewRoundTypes") List<RoundType> interviewRoundTypes,
            org.springframework.data.domain.Pageable pageable);

    @Query(
            """
            SELECT COUNT(ad.id)
            FROM ApplicationDetail ad
            JOIN Application a ON a.id = ad.applicationId
            JOIN Round r ON r.id = ad.roundId
            WHERE a.isDeleted = false
              AND (r.isDeleted = false OR r.isDeleted IS NULL)
              AND a.status = fpt.org.inblue.enums.ApplicationStatus.IN_PROGRESS
              AND ad.status IN (:activeStatuses)
              AND r.roundType IN (:interviewRoundTypes)
            """)
    long countActiveInterviews(
            @Param("activeStatuses") List<ApplicationDetailStatus> activeStatuses,
            @Param("interviewRoundTypes") List<RoundType> interviewRoundTypes);

    @Query("SELECT ad.id as id, ad.applicationId as applicationId, ad.roundId as roundId, ad.status as status, "
            + "ad.finalScore as finalScore, ad.hrScore as hrScore, ad.hrNote as hrNote, ad.aiScore as aiScore, "
            + "ad.finalResult as finalResult, ad.startedAt as startedAt, ad.completedAt as completedAt, "
            + "ad.mentorId as mentorId, ad.assignedMentorIds as assignedMentorIds, ad.sessionId as sessionId, "
            + "ad.aiInterviewSessionId as aiInterviewSessionId, ad.createdAt as createdAt, ad.updatedAt as updatedAt "
            + "FROM ApplicationDetail ad")
    List<ApplicationDetailBasicProjection> findAllProjectedBy();

    @Query("SELECT ad.id as id, ad.applicationId as applicationId, ad.roundId as roundId, ad.status as status, "
            + "ad.finalScore as finalScore, ad.hrScore as hrScore, ad.hrNote as hrNote, ad.aiScore as aiScore, "
            + "ad.finalResult as finalResult, ad.startedAt as startedAt, ad.completedAt as completedAt, "
            + "ad.mentorId as mentorId, ad.assignedMentorIds as assignedMentorIds, ad.sessionId as sessionId, "
            + "ad.aiInterviewSessionId as aiInterviewSessionId, ad.createdAt as createdAt, ad.updatedAt as updatedAt "
            + "FROM ApplicationDetail ad WHERE ad.status = :status")
    List<ApplicationDetailBasicProjection> findAllProjectedByStatus(@Param("status") ApplicationDetailStatus status);

    @Query("SELECT ad FROM ApplicationDetail ad " + "JOIN Round r ON ad.roundId = r.id "
            + "JOIN Application a ON ad.applicationId = a.id "
            + "WHERE r.reviewerId = :reviewerId "
            + "AND r.isDeleted = false "
            + "AND a.isDeleted = false "
            + "AND (r.isAuto = false OR r.isAuto IS NULL)")
    List<ApplicationDetail> findAllByReviewerId(@Param("reviewerId") Integer reviewerId);

    @Query("SELECT ad FROM ApplicationDetail ad "
            + "JOIN Application a ON a.id = ad.applicationId "
            + "WHERE a.userId = :userId AND a.isDeleted = false")
    List<ApplicationDetail> findAllByUserId(@Param("userId") int userId);
}
