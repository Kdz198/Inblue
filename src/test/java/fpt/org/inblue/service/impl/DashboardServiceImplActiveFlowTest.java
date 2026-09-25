package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplActiveFlowTest {
    @Mock
    MentorRepository mentorRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    PaymentRepository paymentRepository;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(mentorRepository, userRepository, sessionRepository, paymentRepository);
    }

    @Test
    void mentorTotalCountsOnlyActiveMentors() {
        when(mentorRepository.countMentorByIsActive(true)).thenReturn(4);
        assertEquals(4, service.getMentorTotal());
    }

    @Test
    void userTotalCountsOnlyUserRole() {
        when(userRepository.countUserByRole(Role.USER)).thenReturn(8);
        assertEquals(8, service.getUserTotal());
    }

    @Test
    void sessionTotalConvertsRepositoryCount() {
        when(sessionRepository.count()).thenReturn(12L);
        assertEquals(12, service.getSessionTotal());
    }

    @Test
    void paymentsIncludeOnlyCompletedTransactions() {
        Payment payment = new Payment();
        when(paymentRepository.findAllByStatus(PaymentStatus.COMPLETED)).thenReturn(List.of(payment));
        assertEquals(List.of(payment), service.getPayments());
    }

    @Test
    void emptyDashboardCollectionsRemainEmpty() {
        when(paymentRepository.findAllByStatus(PaymentStatus.COMPLETED)).thenReturn(List.of());
        assertEquals(List.of(), service.getPayments());
    }
}
