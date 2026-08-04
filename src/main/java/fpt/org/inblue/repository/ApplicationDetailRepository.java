package fpt.org.inblue.repository;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.response.admin.ApplicationDetailBasicProjection;
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
}
