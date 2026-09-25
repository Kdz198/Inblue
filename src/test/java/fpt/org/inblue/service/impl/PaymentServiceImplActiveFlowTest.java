package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.utils.SecurityUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.PayOS;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplActiveFlowTest {
    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PayOS payOS;

    @Mock
    UserRepository userRepository;

    @Mock
    JdPurchaseRepository purchaseRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    SecurityUtils securityUtils;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(
                paymentRepository, payOS, userRepository, purchaseRepository, jobRepository, securityUtils);
    }

    @Test
    void getPaymentReturnsExistingPayment() {
        Payment payment = new Payment();
        payment.setId(1);
        when(paymentRepository.findById(1)).thenReturn(payment);
        assertSame(payment, service.getPayment(1));
    }

    @Test
    void getPaymentRejectsUnknownPayment() {
        when(paymentRepository.findById(9)).thenReturn(null);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getPayment(9))
                        .getStatus()
                        .value());
    }

    @Test
    void getPaymentsReturnsRepositoryData() {
        Payment payment = new Payment();
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        assertEquals(List.of(payment), service.getPayments());
    }

    @Test
    void cancelPaymentRejectsUnknownTransaction() {
        when(paymentRepository.findByTransactionCode("missing")).thenReturn(null);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.cancelPayment("missing"))
                        .getStatus()
                        .value());
    }

    @Test
    void cancelPaymentMarksExistingPaymentFailed() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByTransactionCode("tx")).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        assertSame(payment, service.cancelPayment("tx"));
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }
}
