package fpt.org.inblue.repository;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.model.MentorInterviewBooking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorInterviewBookingRepository extends JpaRepository<MentorInterviewBooking, Long> {
    Optional<MentorInterviewBooking> findBySessionKey(String sessionKey);
    Optional<MentorInterviewBooking> findBySessionId(Integer sessionId);
    List<MentorInterviewBooking> findAllByStatus(BookingStatus status);
    List<MentorInterviewBooking> findAllByApplicantUserId(int applicantUserId);

    @Query("SELECT b FROM MentorInterviewBooking b WHERE b.kioskId = :kioskId AND b.status <> :cancelledStatus AND b.scheduledStart >= :start AND b.scheduledEnd <= :end")
    List<MentorInterviewBooking> findActiveBookingsByKioskAndDateRange(
            @Param("kioskId") Long kioskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") BookingStatus cancelledStatus
    );

    @Query("SELECT COUNT(b) FROM MentorInterviewBooking b WHERE b.kioskId = :kioskId AND b.status <> :cancelledStatus AND b.scheduledStart < :end AND b.scheduledEnd > :start")
    long countOverlappingBookingsForKiosk(
            @Param("kioskId") Long kioskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") BookingStatus cancelledStatus
    );

    @Query("SELECT COUNT(b) FROM MentorInterviewBooking b WHERE b.mentorId = :mentorId AND b.status <> :cancelledStatus AND b.scheduledStart < :end AND b.scheduledEnd > :start")
    long countOverlappingBookingsForMentor(
            @Param("mentorId") int mentorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") BookingStatus cancelledStatus
    );
}
