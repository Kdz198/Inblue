package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import fpt.org.inblue.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserScheduleServiceImplActiveFlowTest {
    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    RoundRepository roundRepository;

    @Mock
    KioskBookingRepository bookingRepository;

    @Mock
    KioskRepository kioskRepository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MentorRepository mentorRepository;

    private UserScheduleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserScheduleServiceImpl(
                detailRepository,
                applicationRepository,
                jobRepository,
                roundRepository,
                bookingRepository,
                kioskRepository,
                sessionRepository,
                userRepository,
                mentorRepository);
    }

    @Test
    void emptyUserScheduleHasNoEvents() {
        when(detailRepository.findAllByUserId(7)).thenReturn(List.of());
        when(bookingRepository.findAllByApplicantUserId(7)).thenReturn(List.of());
        when(sessionRepository.findAllByUserId(7)).thenReturn(List.of());
        assertEquals(
                List.of(),
                service.getUserSchedule(
                        7, LocalDateTime.now(), LocalDateTime.now().plusDays(7)));
    }

    @Test
    void emptyMentorScheduleHasNoEvents() {
        when(detailRepository.findAllByMentorId(3)).thenReturn(List.of());
        when(sessionRepository.findAllByUserId2(3)).thenReturn(List.of());
        assertEquals(
                List.of(),
                service.getMentorSchedule(
                        3, LocalDateTime.now(), LocalDateTime.now().plusDays(7)));
    }
}
