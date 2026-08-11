package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.response.UserScheduleEventDto;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.UserScheduleService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserScheduleServiceImpl implements UserScheduleService {

    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final RoundRepository roundRepository;
    private final KioskBookingRepository kioskBookingRepository;
    private final KioskRepository kioskRepository;
    private final SessionRepository sessionRepository;

    @Override
    public List<UserScheduleEventDto> getUserSchedule(int userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<UserScheduleEventDto> events = new ArrayList<>();

        // 1. Lấy thông tin các vòng thi / phỏng vấn ứng tuyển (ApplicationDetail)
        List<ApplicationDetail> appDetails = applicationDetailRepository.findAllByUserId(userId);
        for (ApplicationDetail ad : appDetails) {
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (ad.getSessionInfo() != null) {
                start = ad.getSessionInfo().getStartTime();
                end = ad.getSessionInfo().getEndTime();
            }

            if (start == null && ad.getStartedAt() != null) {
                start = ad.getStartedAt();
            }
            if (end == null && ad.getCompletedAt() != null) {
                end = ad.getCompletedAt();
            }

            // Nếu không có thời gian bắt đầu thì không hiển thị trên lịch
            if (start == null) {
                continue;
            }

            // Nếu end rỗng, mặc định lấy start + 60 phút
            if (end == null) {
                end = start.plusMinutes(60);
            }

            // Filter theo dải thời gian
            if (!isWithinTimeRange(start, end, startDate, endDate)) {
                continue;
            }

            Application app =
                    applicationRepository.findById(ad.getApplicationId()).orElse(null);
            JobDescription jd = (app != null && app.getJdId() != null)
                    ? jobDescriptionRepository.findById(app.getJdId()).orElse(null)
                    : null;
            Round round = (ad.getRoundId() != null)
                    ? roundRepository.findById(ad.getRoundId()).orElse(null)
                    : null;

            String roundName = (round != null && round.getName() != null) ? round.getName() : "Vòng tuyển dụng";
            String jobTitle = (jd != null && jd.getTitle() != null) ? jd.getTitle() : "";
            String title = roundName + (!jobTitle.isEmpty() ? " - " + jobTitle : "");

            String meetingType =
                    (ad.getSessionInfo() != null && ad.getSessionInfo().getMeetingType() != null)
                            ? ad.getSessionInfo().getMeetingType().name()
                            : "ONLINE";

            UserScheduleEventDto event = UserScheduleEventDto.builder()
                    .id("APP_DETAIL_" + ad.getId())
                    .title(title)
                    .description(
                            round != null && round.getConfigData() != null
                                    ? round.getConfigData().getInstruction()
                                    : null)
                    .eventType("APPLICATION_ROUND")
                    .start(start)
                    .end(end)
                    .status(ad.getStatus() != null ? ad.getStatus().name() : "PENDING")
                    .meetingType(meetingType)
                    .location(meetingType)
                    .color("#3B82F6") // Blue badge
                    .applicationId(ad.getApplicationId())
                    .applicationDetailId(ad.getId())
                    .sessionId(ad.getSessionId())
                    .jobTitle(jobTitle)
                    .roundName(roundName)
                    .build();

            events.add(event);
        }

        // 2. Lấy thông tin các lịch đặt Kiosk (KioskBooking)
        List<KioskBooking> kioskBookings = kioskBookingRepository.findAllByApplicantUserId(userId);
        for (KioskBooking kb : kioskBookings) {
            if (kb.getStatus() == BookingStatus.CANCELLED) {
                continue; // Bỏ qua lịch đã hủy
            }

            LocalDateTime start = kb.getScheduledStart();
            LocalDateTime end = kb.getScheduledEnd();

            if (start == null) {
                continue;
            }
            if (end == null) {
                end = start.plusMinutes(60);
            }

            if (!isWithinTimeRange(start, end, startDate, endDate)) {
                continue;
            }

            Kiosk kiosk = (kb.getKioskId() != null)
                    ? kioskRepository.findById(kb.getKioskId()).orElse(null)
                    : null;

            String kioskName = (kiosk != null && kiosk.getName() != null) ? kiosk.getName() : "Trạm Kiosk";
            String kioskLocation = (kiosk != null && kiosk.getLocation() != null) ? kiosk.getLocation() : "";
            String locationStr = kioskName + (!kioskLocation.isEmpty() ? " (" + kioskLocation + ")" : "");

            UserScheduleEventDto event = UserScheduleEventDto.builder()
                    .id("KIOSK_BOOKING_" + kb.getId())
                    .title("Lịch phỏng vấn Kiosk - " + kioskName)
                    .description(kb.getNotes())
                    .eventType("KIOSK_BOOKING")
                    .start(start)
                    .end(end)
                    .status(kb.getStatus() != null ? kb.getStatus().name() : "CONFIRMED")
                    .meetingType("KIOSK")
                    .location(locationStr)
                    .color("#10B981") // Green badge
                    .kioskId(kb.getKioskId())
                    .sessionKey(kb.getSessionKey())
                    .applicationDetailId(kb.getApplicationDetailId())
                    .build();

            events.add(event);
        }

        // 3. Lấy thông tin các buổi họp 1:1 (Session)
        List<Session> sessions = sessionRepository.findAllByUserIdOrUserId2(userId, userId);
        for (Session sess : sessions) {
            Timestamp rawStart = (sess.getUserId() == userId) ? sess.getStartTime1() : sess.getStartTime2();
            if (rawStart == null) {
                rawStart = sess.getJoinTime();
            }

            if (rawStart == null) {
                continue;
            }

            LocalDateTime start =
                    rawStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            Timestamp rawEnd = (sess.getUserId() == userId) ? sess.getEndTime1() : sess.getEndTime2();
            LocalDateTime end = null;
            if (rawEnd != null) {
                end = rawEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else if (sess.getDuration() != null && sess.getDuration() > 0) {
                end = start.plusMinutes(sess.getDuration());
            } else {
                end = start.plusMinutes(60);
            }

            if (!isWithinTimeRange(start, end, startDate, endDate)) {
                continue;
            }

            UserScheduleEventDto event = UserScheduleEventDto.builder()
                    .id("SESSION_" + sess.getId())
                    .title("Phòng họp Mentor 1:1" + (sess.getRoomName() != null ? " - " + sess.getRoomName() : ""))
                    .description("Buổi trao đổi phỏng vấn/review trực tuyến")
                    .eventType("MENTOR_SESSION")
                    .start(start)
                    .end(end)
                    .status(sess.getStatus() != null ? sess.getStatus().name() : "SCHEDULED")
                    .meetingType("ONLINE")
                    .location(sess.getRoomUrl())
                    .roomUrl(sess.getRoomUrl())
                    .color("#8B5CF6") // Purple badge
                    .sessionId(sess.getId())
                    .sessionKey(sess.getSessionKey())
                    .build();

            events.add(event);
        }

        // Sắp xếp tăng dần theo thời gian bắt đầu (start)
        events.sort(Comparator.comparing(UserScheduleEventDto::getStart));

        return events;
    }

    private boolean isWithinTimeRange(
            LocalDateTime start, LocalDateTime end, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && end.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && start.isAfter(endDate)) {
            return false;
        }
        return true;
    }
}
