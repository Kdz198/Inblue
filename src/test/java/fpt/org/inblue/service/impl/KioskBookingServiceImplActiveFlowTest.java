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
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApplicationDetailService;
import fpt.org.inblue.service.NotificationService;
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
}
