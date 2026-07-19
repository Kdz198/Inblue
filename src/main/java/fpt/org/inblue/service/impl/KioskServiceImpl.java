package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskBooking;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.model.dto.SlotDto;
import fpt.org.inblue.repository.KioskBookingRepository;
import fpt.org.inblue.repository.KioskRepository;
import fpt.org.inblue.repository.KioskScheduleRepository;
import fpt.org.inblue.service.KioskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KioskServiceImpl implements KioskService {

    private final KioskRepository kioskRepository;
    private final KioskScheduleRepository kioskScheduleRepository;
    private final KioskBookingRepository bookingRepository;

    @Override
    public List<Kiosk> getActiveKiosks() {
        return kioskRepository.findAllByIsActiveTrue();
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
        KioskSchedule existing = kioskScheduleRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("KioskSchedule not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!kioskRepository.existsById(schedule.getKioskId())) {
            throw new CustomException("Kiosk not found with id: " + schedule.getKioskId(), HttpStatus.NOT_FOUND);
        }

        existing.setKioskId(schedule.getKioskId());
        existing.setDayOfWeek(schedule.getDayOfWeek());
        existing.setOpenTime(schedule.getOpenTime());
        existing.setCloseTime(schedule.getCloseTime());
        existing.setSlotDurationMinutes(schedule.getSlotDurationMinutes());
        existing.setActive(schedule.isActive());
        return kioskScheduleRepository.save(existing);
    }
}
