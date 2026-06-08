package fpt.org.inblue.repository;

import fpt.org.inblue.model.ApplicationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationDetailRepository extends JpaRepository<ApplicationDetail, Long> {
    ApplicationDetail findByApplicationId(Long applicationId);

    List<ApplicationDetail> findAllByApplicationId(Long applicationId);
}
