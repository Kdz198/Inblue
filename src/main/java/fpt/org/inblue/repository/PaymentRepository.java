package fpt.org.inblue.repository;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findById(int id);

    List<Payment> findByStatusAndPayAtBefore(PaymentStatus status, LocalDateTime payAtBefore);
}
