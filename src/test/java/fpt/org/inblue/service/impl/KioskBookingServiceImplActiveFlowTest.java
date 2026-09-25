package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.KioskBooking;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.NotificationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KioskBookingServiceImplActiveFlowTest {
    @Mock
    KioskBookingRepository repository;

    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    KioskRepository kioskRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    RoundRepository roundRepository;

    @Mock
    ApplicationDetailService detailService;

    private KioskBookingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KioskBookingServiceImpl(
                repository,
                detailRepository,
                kioskRepository,
                userRepository,
                notificationService,
                roundRepository,
                detailService);
    }

    @Test
    void findByApplicationDetailIdReturnsBooking() {
        KioskBooking booking = KioskBooking.builder().id(1L).build();
        when(repository.findByApplicationDetailId(2L)).thenReturn(Optional.of(booking));
        assertSame(booking, service.findByApplicationDetailId(2L));
    }

    @Test
    void cancelBookingRejectsUnknownBooking() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.cancelBooking(9L, 7))
                        .getStatus()
                        .value());
    }

    @Test
    void cancelBookingRejectsCompletedBooking() {
        KioskBooking booking = KioskBooking.builder()
                .id(1L)
                .applicantUserId(7)
                .status(BookingStatus.COMPLETED)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(booking));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.cancelBooking(1L, 7))
                        .getStatus()
                        .value());
    }

    @Test
    void cancelBookingResetsApplicationDetail() {
        KioskBooking booking = KioskBooking.builder()
                .id(1L)
                .applicationDetailId(2L)
                .applicantUserId(7)
                .status(BookingStatus.ROOM_CREATED)
                .build();
        ApplicationDetail detail = ApplicationDetail.builder().id(2L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(booking));
        when(detailRepository.findById(2L)).thenReturn(Optional.of(detail));
        service.cancelBooking(1L, 7);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(detailRepository).save(detail);
    }

    @Test
    void pickSlotRejectsOverlappingBooking() {
        PickSlotDtoRequest request = pickSlotRequest();
        when(detailRepository.findById(2L))
                .thenReturn(Optional.of(ApplicationDetail.builder().id(2L).build()));
        when(kioskRepository.existsById(1L)).thenReturn(true);
        when(repository.countOverlappingBookingsForKiosk(
                        1L, request.getScheduledStart(), request.getScheduledEnd(), BookingStatus.CANCELLED))
                .thenReturn(1L);
        assertEquals(
                409,
                assertThrows(CustomException.class, () -> service.pickSlot(request, 7))
                        .getStatus()
                        .value());
    }

    @Test
    void pickSlotRejectsNonAiRound() {
        PickSlotDtoRequest request = pickSlotRequest();
        ApplicationDetail detail =
                ApplicationDetail.builder().id(2L).roundId(3L).build();
        when(detailRepository.findById(2L)).thenReturn(Optional.of(detail));
        when(kioskRepository.existsById(1L)).thenReturn(true);
        when(repository.countOverlappingBookingsForKiosk(
                        1L, request.getScheduledStart(), request.getScheduledEnd(), BookingStatus.CANCELLED))
                .thenReturn(0L);
        when(roundRepository.findById(3L))
                .thenReturn(Optional.of(Round.builder()
                        .id(3L)
                        .roundType(fpt.org.inblue.enums.RoundType.QUIZ)
                        .build()));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.pickSlot(request, 7))
                        .getStatus()
                        .value());
    }

    @Test
    void enterKioskRejectsCancelledBooking() {
        KioskBooking booking = KioskBooking.builder()
                .sessionKey("pin")
                .kioskId(1L)
                .status(BookingStatus.CANCELLED)
                .build();
        when(repository.findBySessionKey("pin")).thenReturn(Optional.of(booking));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.enterKiosk("pin", 1L))
                        .getStatus()
                        .value());
    }

    private PickSlotDtoRequest pickSlotRequest() {
        PickSlotDtoRequest request = new PickSlotDtoRequest();
        request.setApplicationDetailId(2L);
        request.setKioskId(1L);
        request.setScheduledStart(LocalDateTime.of(2026, 9, 25, 10, 0));
        request.setScheduledEnd(LocalDateTime.of(2026, 9, 25, 11, 0));
        return request;
    }
}
