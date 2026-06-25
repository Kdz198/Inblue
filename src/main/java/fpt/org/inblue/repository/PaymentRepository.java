package fpt.org.inblue.repository;

import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.model.Payment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findById(int id);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime payAtBefore);

    Payment findByTransactionCode(String transactionCode);

    List<Payment> findAllByStatus(PaymentStatus status);
}
