package fpt.org.inblue.repository;

import fpt.org.inblue.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findById(int id);
}
