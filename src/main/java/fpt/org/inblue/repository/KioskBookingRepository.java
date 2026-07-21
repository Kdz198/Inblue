package fpt.org.inblue.repository;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.model.KioskBooking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KioskBookingRepository extends JpaRepository<KioskBooking, Long> {
    Optional<KioskBooking> findBySessionKey(String sessionKey);

    List<KioskBooking> findAllByStatus(BookingStatus status);

    List<KioskBooking> findAllByApplicantUserId(int applicantUserId);

    @Query(
            "SELECT b FROM KioskBooking b WHERE b.kioskId = :kioskId AND b.status <> :cancelledStatus AND b.scheduledStart >= :start AND b.scheduledEnd <= :end")
    List<KioskBooking> findActiveBookingsByKioskAndDateRange(
            @Param("kioskId") Long kioskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") BookingStatus cancelledStatus);

    @Query(
            "SELECT COUNT(b) FROM KioskBooking b WHERE b.kioskId = :kioskId AND b.status <> :cancelledStatus AND b.scheduledStart < :end AND b.scheduledEnd > :start")
    long countOverlappingBookingsForKiosk(
            @Param("kioskId") Long kioskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") BookingStatus cancelledStatus);
}
