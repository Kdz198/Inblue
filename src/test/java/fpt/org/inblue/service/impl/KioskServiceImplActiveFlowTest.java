package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.repository.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KioskServiceImplActiveFlowTest {
    @Mock
    KioskRepository kioskRepository;

    @Mock
    KioskScheduleRepository scheduleRepository;

    @Mock
    KioskBookingRepository bookingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CandidateProfileRepository profileRepository;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    CompanyRepository companyRepository;

    private KioskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KioskServiceImpl(
                kioskRepository,
                scheduleRepository,
                bookingRepository,
                userRepository,
                profileRepository,
                detailRepository,
                applicationRepository,
                jobRepository,
                companyRepository);
    }

    @Test
    void getAllKioskReturnsRepositoryData() {
        Kiosk kiosk = Kiosk.builder().id(1L).build();
        when(kioskRepository.findAll()).thenReturn(List.of(kiosk));
        assertEquals(List.of(kiosk), service.getAllKiosk());
    }

    @Test
    void createKioskPersistsEntity() {
        Kiosk kiosk = Kiosk.builder().name("Lobby").build();
        when(kioskRepository.save(kiosk)).thenReturn(kiosk);
        assertSame(kiosk, service.createKiosk(kiosk));
    }

    @Test
    void createScheduleRejectsUnknownKiosk() {
        KioskSchedule schedule = schedule();
        when(kioskRepository.existsById(1L)).thenReturn(false);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.createSchedule(schedule))
                        .getStatus()
                        .value());
    }

    @Test
    void createScheduleRejectsClosingAfterBoundary() {
        KioskSchedule schedule = schedule();
        schedule.setCloseTime(LocalTime.of(23, 1));
        when(kioskRepository.existsById(1L)).thenReturn(true);
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.createSchedule(schedule))
                        .getStatus()
                        .value());
    }

    @Test
    void availableSlotsReturnEmptyWhenNoSchedule() {
        when(kioskRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.findAllByKioskIdAndDayOfWeekAndIsActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of());
        assertEquals(List.of(), service.getAvailableSlots(1L, LocalDate.of(2026, 9, 14)));
    }

    private KioskSchedule schedule() {
        return KioskSchedule.builder()
                .kioskId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(17, 0))
                .slotDurationMinutes(45)
                .isActive(true)
                .build();
    }
}
