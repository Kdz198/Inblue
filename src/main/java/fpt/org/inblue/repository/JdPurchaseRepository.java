package fpt.org.inblue.repository;

import fpt.org.inblue.enums.JdPurchaseStatus;
import fpt.org.inblue.model.JdPurchase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JdPurchaseRepository extends JpaRepository<JdPurchase, Long> {

    Optional<JdPurchase> findByUserIdAndJdIdAndStatus(int userId, Long jdId, JdPurchaseStatus status);

    boolean existsByUserIdAndJdIdAndStatus(int userId, Long jdId, JdPurchaseStatus status);

    List<JdPurchase> findAllByUserId(int userId);
}
