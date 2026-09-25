package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.JdPurchaseStatus;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JdPurchase;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JdPurchaseServiceImplActiveFlowTest {
    @Mock
    JdPurchaseRepository purchaseRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    SecurityUtils securityUtils;

    private JdPurchaseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JdPurchaseServiceImpl(
                purchaseRepository, jobRepository, companyRepository, paymentRepository, securityUtils);
    }

    @Test
    void hasPurchasedReturnsTrueForPurchasedRecord() {
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        when(purchaseRepository.existsByUserIdAndJdIdAndStatus(7, 3L, JdPurchaseStatus.PURCHASED))
                .thenReturn(true);
        assertTrue(service.hasPurchased(3L));
    }

    @Test
    void hasPurchasedAllowsOpenFreeJob() {
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        JobDescription job = JobDescription.builder()
                .status(JobDescriptionStatus.OPEN)
                .isDeleted(false)
                .price(0L)
                .build();
        when(jobRepository.findById(3L)).thenReturn(Optional.of(job));
        assertTrue(service.hasPurchased(3L));
    }

    @Test
    void hasPurchasedRejectsClosedOrPaidJobWithoutPurchase() {
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        JobDescription job = JobDescription.builder()
                .status(JobDescriptionStatus.CLOSED)
                .price(1000L)
                .build();
        when(jobRepository.findById(3L)).thenReturn(Optional.of(job));
        assertFalse(service.hasPurchased(3L));
    }

    @Test
    void getPurchaseRejectsMissingEntitlement() {
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        when(purchaseRepository.findByUserIdAndJdIdAndStatus(7, 3L, JdPurchaseStatus.PURCHASED))
                .thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getPurchase(3L))
                        .getStatus()
                        .value());
    }

    @Test
    void getMyPurchasesMarksExpiredAndEnrichesJobAndPayment() {
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        JdPurchase purchase = JdPurchase.builder()
                .id(1L)
                .jdId(3L)
                .paymentId(4)
                .status(JdPurchaseStatus.PURCHASED)
                .purchasedAt(LocalDateTime.now().minusDays(31))
                .build();
        JobDescription job = JobDescription.builder().id(3L).title("Backend").build();
        Company company = Company.builder().name("Acme").logoUrl("logo").build();
        Payment payment = new Payment();
        payment.setId(4);
        payment.setAmount(1000L);
        when(purchaseRepository.findAllByUserId(7)).thenReturn(List.of(purchase));
        when(jobRepository.findById(3L)).thenReturn(Optional.of(job));
        when(companyRepository.findByJobDescriptionsId(3L)).thenReturn(Optional.of(company));
        when(paymentRepository.findById(4)).thenReturn(payment);
        var result = service.getMyPurchases().getFirst();
        assertEquals(JdPurchaseStatus.EXPIRED, result.getStatus());
        assertEquals("Acme", result.getJobDescription().getCompanyName());
        assertEquals("VND", result.getPayment().getCurrency());
    }
}
