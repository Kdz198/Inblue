package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.KioskBookingService;
import fpt.org.inblue.service.NotificationService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KioskBookingServiceImpl implements KioskBookingService {

    private final KioskBookingRepository bookingRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final KioskRepository kioskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RoundRepository roundRepository;
    private final ApplicationDetailService applicationDetailService;

    @Override
    @Transactional
    public KioskBooking pickSlot(PickSlotDtoRequest dto, int userId) {
        ApplicationDetail appDetail = applicationDetailRepository
                .findById(dto.getApplicationDetailId())
                .orElseThrow(() -> new CustomException("ApplicationDetail not found", HttpStatus.NOT_FOUND));

        if (!kioskRepository.existsById(dto.getKioskId())) {
            throw new CustomException("Kiosk not found", HttpStatus.NOT_FOUND);
        }

        // Check if slot is overlapping at the kiosk
        long count = bookingRepository.countOverlappingBookingsForKiosk(
                dto.getKioskId(), dto.getScheduledStart(), dto.getScheduledEnd(), BookingStatus.CANCELLED);
        if (count > 0) {
            throw new CustomException("Selected slot is already booked", HttpStatus.CONFLICT);
        }

        // Create booking
        Round round = roundRepository
                .findById(appDetail.getRoundId())
                .orElseThrow(() -> new CustomException("Round not found", HttpStatus.NOT_FOUND));

        if (round.getRoundType() != fpt.org.inblue.enums.RoundType.AI_INTERVIEW) {
            throw new CustomException("Kiosk is only available for AI Interview rounds", HttpStatus.BAD_REQUEST);
        }

        String pin = String.format("%06d", new java.util.Random().nextInt(1000000));
        KioskBooking booking = KioskBooking.builder()
                .applicationDetailId(dto.getApplicationDetailId())
                .kioskId(dto.getKioskId())
                .applicantUserId(userId)
                .scheduledStart(dto.getScheduledStart())
                .scheduledEnd(dto.getScheduledEnd())
                .status(BookingStatus.ROOM_CREATED)
                .sessionKey(pin)
                .build();
        booking = bookingRepository.save(booking);

        appDetail.setStatus(ApplicationDetailStatus.SLOT_PICKED);
        applicationDetailRepository.save(appDetail);

        User candidate = userRepository.findById(userId).orElse(null);
        if (candidate != null) {
            Notification notif = new Notification();
            notif.setUser(candidate);
            notif.setTitle("Lịch phỏng vấn AI Interview tại Kiosk");
            notif.setMessage("Bạn đã đăng ký thành công phỏng vấn AI tại Kiosk " + dto.getKioskId()
                    + " lúc " + dto.getScheduledStart().toString()
                    + ". MÃ PIN VÀO PHÒNG CỦA BẠN LÀ: " + pin);
            notif.setIsRead(false);
            notificationService.createNotification(notif);
        }

        return booking;
    }

    @Override
    public KioskBooking findByApplicationDetailId(Long applicationDetailId) {

        return bookingRepository.findByApplicationDetailId(applicationDetailId).get();
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, int userId) {
        KioskBooking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found", HttpStatus.NOT_FOUND));

        // Security check: only allow applicant or admin/staff to cancel
        if (booking.getApplicantUserId() != userId) {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.STAFF) {
                throw new CustomException("Unauthorized to cancel this booking", HttpStatus.UNAUTHORIZED);
            }
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CustomException("Cannot cancel booking in current state", HttpStatus.BAD_REQUEST);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Reset application detail status
        ApplicationDetail appDetail = applicationDetailRepository
                .findById(booking.getApplicationDetailId())
                .orElse(null);
        if (appDetail != null) {
            appDetail.setStatus(ApplicationDetailStatus.PENDING);
            applicationDetailRepository.save(appDetail);
        }
    }

    @Override
    @Transactional
    public KioskEnterDtoResponse enterKiosk(String sessionKey, Long kioskId) {
        KioskBooking booking = bookingRepository
                .findBySessionKey(sessionKey)
                .orElseThrow(() -> new CustomException("Booking not found for this session key", HttpStatus.NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CustomException("Booking has been cancelled", HttpStatus.BAD_REQUEST);
        }

        if (booking.getKioskId() != kioskId) {
            throw new CustomException("This booking is not for the specified kiosk", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime start = booking.getScheduledStart();
        if (now.isBefore(start.minusMinutes(15)) || now.isAfter(start.plusMinutes(15))) {
            throw new CustomException(
                    "You can only enter the Kiosk within 15 minutes of your scheduled start time (" + start + ")",
                    HttpStatus.BAD_REQUEST);
        }

        ApplicationDetail appDetail = applicationDetailRepository
                .findById(booking.getApplicationDetailId())
                .orElseThrow(() -> new CustomException("ApplicationDetail not found", HttpStatus.NOT_FOUND));
        Round round = roundRepository
                .findById(appDetail.getRoundId())
                .orElseThrow(() -> new CustomException("Round not found", HttpStatus.NOT_FOUND));

        if (round.getRoundType() != fpt.org.inblue.enums.RoundType.AI_INTERVIEW) {
            throw new CustomException("Kiosk only supports AI Interview rounds", HttpStatus.BAD_REQUEST);
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);

        String aiSessionKey = applicationDetailService.startAiInterview(appDetail.getId());
        appDetail.setStatus(fpt.org.inblue.enums.ApplicationDetailStatus.PENDING);
        applicationDetailRepository.save(appDetail);

        int durationMinutes = round.getConfigData().getTimeLimitMinutes();

        return KioskEnterDtoResponse.builder()
                .aiSessionKey(aiSessionKey)
                .durationMinutes(durationMinutes)
                .build();
    }
}
