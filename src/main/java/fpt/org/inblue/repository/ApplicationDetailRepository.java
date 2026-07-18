package fpt.org.inblue.repository;

import fpt.org.inblue.model.ApplicationDetail;
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

    @Query("SELECT ad FROM ApplicationDetail ad " + "JOIN Round r ON ad.roundId = r.id "
            + "JOIN Application a ON ad.applicationId = a.id "
            + "WHERE r.reviewerId = :reviewerId "
            + "AND r.isDeleted = false "
            + "AND a.isDeleted = false "
            + "AND (r.isAuto = false OR r.isAuto IS NULL)")
    List<ApplicationDetail> findAllByReviewerId(@Param("reviewerId") Integer reviewerId);
}
