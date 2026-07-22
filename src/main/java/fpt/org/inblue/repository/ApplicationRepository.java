package fpt.org.inblue.repository;

import fpt.org.inblue.model.Application;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByUserId(int userId);

    List<Application> findByJdIdAndIsDeletedFalse(Long jdId);

    List<Application> findByJdId(Long jdId);
}
