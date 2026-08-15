package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskBooking;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.SlotDto;
import fpt.org.inblue.model.dto.response.KioskHistoryResponseDto;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.CandidateProfileRepository;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.KioskBookingRepository;
import fpt.org.inblue.repository.KioskRepository;
import fpt.org.inblue.repository.KioskScheduleRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.KioskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KioskServiceImpl implements KioskService {

    private final KioskRepository kioskRepository;
    private final KioskScheduleRepository kioskScheduleRepository;
    private final KioskBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationRepository applicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final CompanyRepository companyRepository;

    @Override
    public List<Kiosk> getAllKiosk() {
        return kioskRepository.findAll();
    }

    @Override
    public Kiosk createKiosk(Kiosk kiosk) {
        return kioskRepository.save(kiosk);
    }

    @Override
    public KioskSchedule createSchedule(KioskSchedule schedule) {
        if (!kioskRepository.existsById(schedule.getKioskId())) {
            throw new CustomException("Kiosk not found with id: " + schedule.getKioskId(), HttpStatus.NOT_FOUND);
        }
        LocalTime maxAllowedCloseTime = LocalTime.of(22, 0); // 22:00:00
        if (schedule.getCloseTime() != null && schedule.getCloseTime().isAfter(maxAllowedCloseTime)) {
            throw new CustomException("Giờ đóng cửa không được sau 22:00", HttpStatus.BAD_REQUEST);
        }

        // 3. (Khuyến nghị) Đảm bảo giờ mở cửa phải trước giờ đóng cửa
        if (schedule.getOpenTime() != null
                && schedule.getCloseTime() != null
                && !schedule.getOpenTime().isBefore(schedule.getCloseTime())) {
            throw new CustomException("Giờ mở cửa phải trước giờ đóng cửa", HttpStatus.BAD_REQUEST);
        }
        return kioskScheduleRepository.save(schedule);
    }

    @Override
    public List<KioskSchedule> getSchedulesByKiosk(Long kioskId) {
        return kioskScheduleRepository.findAllByKioskIdAndIsActiveTrue(kioskId);
    }

    @Override
    public List<SlotDto> getAvailableSlots(Long kioskId, LocalDate date) {
        if (!kioskRepository.existsById(kioskId)) {
            throw new CustomException("Kiosk not found with id: " + kioskId, HttpStatus.NOT_FOUND);
        }

        // 1. Get schedule for the given day of week
        List<KioskSchedule> schedules =
                kioskScheduleRepository.findAllByKioskIdAndDayOfWeekAndIsActiveTrue(kioskId, date.getDayOfWeek());

        List<SlotDto> availableSlots = new ArrayList<>();
        if (schedules.isEmpty()) {
            return availableSlots;
        }

        KioskSchedule schedule = schedules.get(0); // Assume single active schedule per day of week

        // 2. Fetch active bookings on that day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<KioskBooking> bookings = bookingRepository.findActiveBookingsByKioskAndDateRange(
                kioskId, startOfDay, endOfDay, BookingStatus.CANCELLED);

        // 3. Generate slots
        LocalTime current = schedule.getOpenTime();
        LocalTime endLimit = schedule.getCloseTime();
        int duration = schedule.getSlotDurationMinutes();

        while (current.plusMinutes(duration).isBefore(endLimit)
                || current.plusMinutes(duration).equals(endLimit)) {
            LocalDateTime slotStart = date.atTime(current);
            LocalDateTime slotEnd = date.atTime(current.plusMinutes(duration));

            // Check overlap
            boolean isOverlapping = false;
            for (KioskBooking booking : bookings) {
                if (slotStart.isBefore(booking.getScheduledEnd()) && slotEnd.isAfter(booking.getScheduledStart())) {
                    isOverlapping = true;
                    break;
                }
            }

            availableSlots.add(new SlotDto(slotStart, slotEnd, !isOverlapping));
            current = current.plusMinutes(duration + 15);
        }

        return availableSlots;
    }

    @Override
    public Kiosk updateKiosk(Long id, Kiosk kiosk) {
        Kiosk existing = kioskRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Kiosk not found with id: " + id, HttpStatus.NOT_FOUND));
        existing.setName(kiosk.getName());
        existing.setLocation(kiosk.getLocation());
        existing.setActive(kiosk.isActive());
        return kioskRepository.save(existing);
    }

    @Override
    public KioskSchedule updateSchedule(Long id, KioskSchedule schedule) {
        // 1. Tìm bản ghi lịch cũ theo ID
        KioskSchedule existing = kioskScheduleRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("KioskSchedule not found with id: " + id, HttpStatus.NOT_FOUND));

        // 2. Kiểm tra Kiosk có tồn tại hay không
        if (!kioskRepository.existsById(schedule.getKioskId())) {
            throw new CustomException("Kiosk not found with id: " + schedule.getKioskId(), HttpStatus.NOT_FOUND);
        }

        // 3. Chặn giờ đóng cửa sau 22:00
        LocalTime maxAllowedCloseTime = LocalTime.of(22, 0); // 22:00:00
        if (schedule.getCloseTime() != null && schedule.getCloseTime().isAfter(maxAllowedCloseTime)) {
            throw new CustomException("Giờ đóng cửa không được sau 22:00", HttpStatus.BAD_REQUEST);
        }

        // 4. Đảm bảo giờ mở cửa phải trước giờ đóng cửa
        if (schedule.getOpenTime() != null
                && schedule.getCloseTime() != null
                && !schedule.getOpenTime().isBefore(schedule.getCloseTime())) {
            throw new CustomException("Giờ mở cửa phải trước giờ đóng cửa", HttpStatus.BAD_REQUEST);
        }

        existing.setKioskId(schedule.getKioskId());
        existing.setDayOfWeek(schedule.getDayOfWeek());
        existing.setOpenTime(schedule.getOpenTime());
        existing.setCloseTime(schedule.getCloseTime());
        existing.setSlotDurationMinutes(schedule.getSlotDurationMinutes());
        existing.setActive(schedule.isActive());
        return kioskScheduleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KioskHistoryResponseDto> getKioskHistory(Long kioskId) {
        if (!kioskRepository.existsById(kioskId)) {
            throw new CustomException("Kiosk not found with id: " + kioskId, HttpStatus.NOT_FOUND);
        }

        List<KioskBooking> bookings = bookingRepository.findAllByKioskIdOrderByScheduledStartDesc(kioskId);
        List<KioskHistoryResponseDto> result = new ArrayList<>();

        for (KioskBooking booking : bookings) {
            Optional<User> userOpt = userRepository.findById(booking.getApplicantUserId());
            String applicantName = userOpt.map(User::getName).orElse("N/A");
            String applicantEmail = userOpt.map(User::getEmail).orElse("N/A");
            String avatarUrl = userOpt.map(User::getAvatarUrl).orElse(null);
            String cvUrl = userOpt.map(User::getCvUrl).orElse(null);

            List<CandidateProfile> profiles = candidateProfileRepository.findByUser_Id(booking.getApplicantUserId());
            CandidateProfile profile = (profiles != null && !profiles.isEmpty()) ? profiles.get(0) : null;
            String targetRole = profile != null ? profile.getTargetRole() : null;
            String targetLevel = profile != null ? profile.getTargetLevel() : null;
            List<String> technicalSkills = profile != null ? profile.getTechnicalSkills() : null;

            KioskHistoryResponseDto.CandidateInfoDto candidateInfoDto =
                    KioskHistoryResponseDto.CandidateInfoDto.builder()
                            .userId(booking.getApplicantUserId())
                            .name(applicantName)
                            .email(applicantEmail)
                            .avatarUrl(avatarUrl)
                            .cvUrl(cvUrl)
                            .targetRole(targetRole)
                            .targetLevel(targetLevel)
                            .technicalSkills(technicalSkills)
                            .build();

            Long applicationId = null;
            Long jdId = null;
            KioskHistoryResponseDto.JobDescriptionInfoDto jdInfoDto = null;

            if (booking.getApplicationDetailId() != null) {
                Optional<ApplicationDetail> appDetailOpt =
                        applicationDetailRepository.findById(booking.getApplicationDetailId());
                if (appDetailOpt.isPresent()) {
                    applicationId = appDetailOpt.get().getApplicationId();
                    if (applicationId != null) {
                        Optional<Application> appOpt = applicationRepository.findById(applicationId);
                        if (appOpt.isPresent()) {
                            jdId = appOpt.get().getJdId();
                            if (jdId != null) {
                                Optional<JobDescription> jdOpt = jobDescriptionRepository.findById(jdId);
                                if (jdOpt.isPresent()) {
                                    JobDescription jd = jdOpt.get();

                                    Optional<Company> companyOpt =
                                            companyRepository.findByJobDescriptionsId(jd.getId());

                                    jdInfoDto = KioskHistoryResponseDto.JobDescriptionInfoDto.builder()
                                            .jdId(jd.getId())
                                            .title(jd.getTitle())
                                            .level(jd.getLevel())
                                            .salaryMin(jd.getSalaryMin())
                                            .salaryMax(jd.getSalaryMax())
                                            .currency(jd.getCurrency())
                                            .companyId(companyOpt
                                                    .map(Company::getId)
                                                    .orElse(null))
                                            .companyName(companyOpt
                                                    .map(Company::getName)
                                                    .orElse("N/A"))
                                            .companyLogo(companyOpt
                                                    .map(Company::getLogoUrl)
                                                    .orElse(null))
                                            .build();
                                }
                            }
                        }
                    }
                }
            }

            KioskHistoryResponseDto dto = KioskHistoryResponseDto.builder()
                    .bookingId(booking.getId())
                    .kioskId(booking.getKioskId())
                    .applicationDetailId(booking.getApplicationDetailId())
                    .applicationId(applicationId)
                    .candidateInfo(candidateInfoDto)
                    .jobDescriptionInfo(jdInfoDto)
                    .scheduledStart(booking.getScheduledStart())
                    .scheduledEnd(booking.getScheduledEnd())
                    .status(booking.getStatus())
                    .sessionKey(booking.getSessionKey())
                    .notes(booking.getNotes())
                    .createdAt(booking.getCreatedAt())
                    .updatedAt(booking.getUpdatedAt())
                    .build();

            result.add(dto);
        }

        return result;
    }
}
