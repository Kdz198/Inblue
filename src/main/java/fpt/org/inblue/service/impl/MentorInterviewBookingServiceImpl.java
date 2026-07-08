package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.enums.SessionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.dailyco.DailyCoCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.MentorInterviewBookingService;
import fpt.org.inblue.service.NotificationService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MentorInterviewBookingServiceImpl implements MentorInterviewBookingService {

    private final MentorInterviewBookingRepository bookingRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final SessionRepository sessionRepository;
    private final KioskRepository kioskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value("${daily.api.url}")
    private String dailyApiUrl;

    @Value("${daily.api.key}")
    private String dailyApiKey;

    @Override
    @Transactional
    public MentorInterviewBooking pickSlot(PickSlotDtoRequest dto, int userId) {
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
        MentorInterviewBooking booking = MentorInterviewBooking.builder()
                .applicationDetailId(dto.getApplicationDetailId())
                .kioskId(dto.getKioskId())
                .applicantUserId(userId)
                .scheduledStart(dto.getScheduledStart())
                .scheduledEnd(dto.getScheduledEnd())
                .status(BookingStatus.AWAITING_MENTOR)
                .build();
        booking = bookingRepository.save(booking);

        // Update application detail with booking ID
        appDetail.setBookingId(booking.getId());
        applicationDetailRepository.save(appDetail);

        return booking;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, int userId) {
        MentorInterviewBooking booking = bookingRepository
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

        // Cancel Session on Daily.co if room exists
        if (booking.getSessionId() != null) {
            Session session = sessionRepository.findById(booking.getSessionId()).orElse(null);
            if (session != null) {
                session.setStatus(SessionStatus.CANCELED);
                sessionRepository.save(session);
                try {
                    deleteDailyCoRoom(session.getRoomName());
                } catch (Exception e) {
                    System.err.println("Failed to delete Daily.co room: " + e.getMessage());
                }
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Reset application detail status
        ApplicationDetail appDetail = applicationDetailRepository
                .findById(booking.getApplicationDetailId())
                .orElse(null);
        if (appDetail != null) {
            appDetail.setStatus(ApplicationDetailStatus.PENDING);
            appDetail.setBookingId(null);
            appDetail.setSessionId(null);
            applicationDetailRepository.save(appDetail);
        }
    }

    @Override
    public List<MentorInterviewBooking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findAllByStatus(status);
    }

    @Override
    @Transactional
    public MentorInterviewBooking assignMentor(Long bookingId, int mentorId, String notes) {
        MentorInterviewBooking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found", HttpStatus.NOT_FOUND));

        if (booking.getStatus() != BookingStatus.AWAITING_MENTOR) {
            throw new CustomException("Booking is not in AWAITING_MENTOR status", HttpStatus.BAD_REQUEST);
        }

        // Check conflict for mentor
        long overlapCount = bookingRepository.countOverlappingBookingsForMentor(
                mentorId, booking.getScheduledStart(), booking.getScheduledEnd(), BookingStatus.CANCELLED);
        if (overlapCount > 0) {
            throw new CustomException("Mentor has another interview booking at this time", HttpStatus.CONFLICT);
        }

        // 1. Create Daily.co Room
        DailyCoCreationRequest dailyReq = new DailyCoCreationRequest();
        dailyReq.setName("booking-" + bookingId + "-" + System.currentTimeMillis());
        dailyReq.setPrivacy("public");
        DailyCoCreationRequest.Properties props = new DailyCoCreationRequest.Properties();
        props.setMax_participants(2);
        props.setStart_video_off(true);
        props.setStart_audio_off(true);
        props.setEnable_screenshare(true);

        // Set exp to 2 hours after scheduled end
        long exp =
                booking.getScheduledEnd().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toEpochSecond() + 7200;
        props.setExp((int) exp);
        props.setEnable_recording("cloud");
        dailyReq.setProperties(props);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DailyCoCreationRequest> entity = new HttpEntity<>(dailyReq, headers);

        String roomUrl = "";
        String roomName = "";
        try {
            ResponseEntity<SessionResponse> response =
                    restTemplate.exchange(dailyApiUrl + "/rooms", HttpMethod.POST, entity, SessionResponse.class);
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                roomUrl = response.getBody().getUrl();
                roomName = response.getBody().getName();
            } else {
                throw new CustomException(
                        "Failed to create Daily.co room: " + response.getStatusCode(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new CustomException(
                    "Error creating Daily.co room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 2. Create Session
        Session session = new Session();
        session.setRoomName(roomName);
        session.setRoomUrl(roomUrl);
        session.setUserId(booking.getApplicantUserId());
        session.setUserId2(mentorId);
        session.setStatus(SessionStatus.SCHEDULED);

        // Convert to VN timestamp
        long ms = booking.getScheduledStart()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toInstant()
                .toEpochMilli();
        session.setJoinTime(new java.sql.Timestamp(ms));

        int duration = (int) java.time.Duration.between(booking.getScheduledStart(), booking.getScheduledEnd())
                .toMinutes();
        session.setDuration(duration);
        session.setSessionKey(UUID.randomUUID().toString());
        session.setKioskId(booking.getKioskId());
        session = sessionRepository.save(session);

        // 3. Update Booking
        booking.setMentorId(mentorId);
        booking.setSessionId(session.getId());
        booking.setSessionKey(session.getSessionKey());
        booking.setStatus(BookingStatus.ROOM_CREATED);
        booking.setNotes(notes);
        booking = bookingRepository.save(booking);

        // 4. Update ApplicationDetail
        ApplicationDetail appDetail = applicationDetailRepository
                .findById(booking.getApplicationDetailId())
                .orElseThrow(() -> new CustomException("ApplicationDetail not found", HttpStatus.NOT_FOUND));
        appDetail.setSessionId((long) session.getId());
        appDetail.setStatus(ApplicationDetailStatus.SLOT_PICKED);
        applicationDetailRepository.save(appDetail);

        // 5. Send Notification
        User candidate = userRepository.findById(booking.getApplicantUserId()).orElse(null);
        if (candidate != null) {
            Notification notif = new Notification();
            notif.setUser(candidate);
            notif.setTitle("Lịch phỏng vấn Mentor Interview");
            notif.setMessage("Bạn đã được xếp lịch phỏng vấn tại Kiosk " + booking.getKioskId()
                    + " vào lúc " + booking.getScheduledStart().toString()
                    + ". Session Key để vào phòng là: " + session.getSessionKey());
            notif.setIsRead(false);
            notificationService.createNotification(notif);
        }

        return booking;
    }

    @Override
    @Transactional
    public KioskEnterDtoResponse enterKiosk(String sessionKey, Long kioskId) {
        Session session = sessionRepository.findBySessionKey(sessionKey);
        if (session == null) {
            throw new CustomException("Invalid session key", HttpStatus.BAD_REQUEST);
        }

        MentorInterviewBooking booking = bookingRepository
                .findBySessionKey(sessionKey)
                .orElseThrow(() -> new CustomException("Booking not found for this session key", HttpStatus.NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CustomException("Booking has been cancelled", HttpStatus.BAD_REQUEST);
        }

        if (!booking.getKioskId().equals(kioskId)) {
            throw new CustomException(
                    "Session key is registered for Kiosk " + booking.getKioskId(), HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime start = booking.getScheduledStart();
        if (now.isBefore(start.minusMinutes(15)) || now.isAfter(start.plusMinutes(15))) {
            throw new CustomException(
                    "You can only enter the Kiosk within 15 minutes of your scheduled start time (" + start + ")",
                    HttpStatus.BAD_REQUEST);
        }

        String candidateName = "Candidate";
        User user = userRepository.findById(booking.getApplicantUserId()).orElse(null);
        if (user != null) {
            candidateName = user.getName();
        }

        String meetingToken = mintDailyCoToken(session.getRoomName(), candidateName);

        // Update session and booking state
        session.setStatus(SessionStatus.ONGOING);
        session.setStartTime1(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 60 * 60 * 1000))); // VN time
        sessionRepository.save(session);

        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);

        return new KioskEnterDtoResponse(meetingToken, session.getRoomUrl());
    }

    private void deleteDailyCoRoom(String roomName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dailyApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String apiUrl = dailyApiUrl + "/rooms/" + roomName;
        try {
            restTemplate.exchange(apiUrl, HttpMethod.DELETE, entity, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to delete Daily.co room: " + e.getMessage());
        }
    }

    private String mintDailyCoToken(String roomName, String userName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("room_name", roomName);
        properties.put("is_owner", false);
        properties.put("user_name", userName);

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("properties", properties);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(dailyApiUrl + "/meeting-tokens", HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("token");
            } else {
                throw new CustomException(
                        "Failed to mint meeting token from Daily.co", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new CustomException(
                    "Error contacting Daily.co for meeting token: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
