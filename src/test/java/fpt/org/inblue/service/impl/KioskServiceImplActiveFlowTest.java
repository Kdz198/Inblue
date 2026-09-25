package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.repository.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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

    @Test
    void getSchedulesByKioskDelegatesToActiveSchedules() {
        KioskSchedule schedule = schedule();
        when(scheduleRepository.findAllByKioskIdAndIsActiveTrue(1L)).thenReturn(List.of(schedule));
        assertEquals(List.of(schedule), service.getSchedulesByKiosk(1L));
    }

    @Test
    void getAvailableSlotsRejectsUnknownKiosk() {
        when(kioskRepository.existsById(1L)).thenReturn(false);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getAvailableSlots(1L, LocalDate.now()))
                        .getStatus()
                        .value());
    }

    @Test
    void updateKioskCopiesEditableFieldsAndSaves() {
        Kiosk existing = Kiosk.builder().id(1L).name("Old").build();
        Kiosk update = Kiosk.builder().name("New").location("Floor 2").isActive(true).build();
        when(kioskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(kioskRepository.save(existing)).thenReturn(existing);
        assertEquals(existing, service.updateKiosk(1L, update));
        assertEquals("New", existing.getName());
        assertEquals("Floor 2", existing.getLocation());
        verify(kioskRepository).save(existing);
    }

    @Test
    void updateScheduleRejectsReversedOpeningHours() {
        KioskSchedule update = schedule();
        update.setOpenTime(LocalTime.of(18, 0));
        update.setCloseTime(LocalTime.of(17, 0));
        when(scheduleRepository.findById(2L)).thenReturn(Optional.of(schedule()));
        when(kioskRepository.existsById(1L)).thenReturn(true);
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.updateSchedule(2L, update))
                        .getStatus()
                        .value());
    }

    @Test
    void updateScheduleRejectsClosingAfterUpdateBoundary() {
        KioskSchedule update = schedule();
        update.setCloseTime(LocalTime.of(22, 1));
        when(scheduleRepository.findById(2L)).thenReturn(Optional.of(schedule()));
        when(kioskRepository.existsById(1L)).thenReturn(true);
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.updateSchedule(2L, update))
                        .getStatus()
                        .value());
    }

    @Test
    void historyRejectsUnknownKiosk() {
        when(kioskRepository.existsById(8L)).thenReturn(false);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getKioskHistory(8L))
                        .getStatus()
                        .value());
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
