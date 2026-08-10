package fpt.org.inblue.repository;

import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.repository.projection.AdminAnalyticsProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findById(int id);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime payAtBefore);

    Payment findByTransactionCode(String transactionCode);

    List<Payment> findAllByStatus(PaymentStatus status);

    @Query(
            """
            SELECT p.id AS transactionId,
                   p.transactionCode AS transactionCode,
                   p.amount AS amount,
                   p.description AS description,
                   p.status AS status,
                   p.createdAt AS createdAt,
                   u.id AS userId,
                   u.name AS userName,
                   u.email AS userEmail,
                   u.avatarUrl AS avatarUrl,
                   jd.id AS jobId,
                   jd.title AS jobTitle
            FROM Payment p
            LEFT JOIN p.user u
            LEFT JOIN JobDescription jd ON jd.id = p.jdId
            WHERE p.createdAt >= :fromTime
              AND p.createdAt < :toTime
              AND p.status = :status
            ORDER BY p.createdAt DESC
            """)
    List<AdminAnalyticsProjection.RecentTransaction> findRecentTransactions(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("status") PaymentStatus status,
            Pageable pageable);
}
