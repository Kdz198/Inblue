package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.JdPurchaseStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JdPurchase;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.dto.response.MyJdPurchaseResponseDto;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.service.JdPurchaseService;
import fpt.org.inblue.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JdPurchaseServiceImpl implements JdPurchaseService {

    private final JdPurchaseRepository jdPurchaseRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final CompanyRepository companyRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    @Override
    public boolean hasPurchased(Long jdId) {
        int userId = securityUtils.getCurrentUserId();
        return jdPurchaseRepository.existsByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED);
    }

    @Override
    public JdPurchase getPurchase(Long jdId) {
        int userId = securityUtils.getCurrentUserId();
        return jdPurchaseRepository
                .findByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED)
                .orElseThrow(() -> new CustomException("Không tìm thấy gói đã mua cho JD này", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<MyJdPurchaseResponseDto> getMyPurchases() {
        int userId = securityUtils.getCurrentUserId();
        List<JdPurchase> purchases = jdPurchaseRepository.findAllByUserId(userId);

        List<MyJdPurchaseResponseDto> responseList = new ArrayList<>();

        for (JdPurchase purchase : purchases) {
            LocalDateTime purchasedAt = purchase.getPurchasedAt();

            LocalDateTime validUntil = purchasedAt != null ? purchasedAt.plusDays(30) : null;
            JdPurchaseStatus status = purchase.getStatus();
            if (status == JdPurchaseStatus.PURCHASED
                    && validUntil != null
                    && LocalDateTime.now().isAfter(validUntil)) {
                status = JdPurchaseStatus.EXPIRED;
            }

            // Enrich JobDescription
            MyJdPurchaseResponseDto.EnrichedJobDescription enrichedJd = null;
            if (purchase.getJdId() != null) {
                Optional<JobDescription> jdOpt = jobDescriptionRepository.findById(purchase.getJdId());
                if (jdOpt.isPresent()) {
                    JobDescription jd = jdOpt.get();
                    Optional<Company> companyOpt = companyRepository.findByJobDescriptionsId(jd.getId());

                    enrichedJd = MyJdPurchaseResponseDto.EnrichedJobDescription.builder()
                            .id(jd.getId())
                            .title(jd.getTitle())
                            .companyName(companyOpt.map(Company::getName).orElse(null))
                            .thumbnailUrl(companyOpt.map(Company::getLogoUrl).orElse(null))
                            .build();
                }
            }

            // Enrich Payment
            MyJdPurchaseResponseDto.EnrichedPayment enrichedPayment = null;
            Payment payment = paymentRepository.findById(purchase.getPaymentId());
            if (payment != null) {
                enrichedPayment = MyJdPurchaseResponseDto.EnrichedPayment.builder()
                        .id(payment.getId())
                        .amount(payment.getAmount())
                        .currency("VND")
                        .method("BANKING")
                        .build();
            }

            MyJdPurchaseResponseDto dto = MyJdPurchaseResponseDto.builder()
                    .id(purchase.getId())
                    .status(status)
                    .purchasedAt(purchasedAt)
                    .usedAt(purchase.getUsedAt())
                    .validUntil(validUntil)
                    .jobDescription(enrichedJd)
                    .payment(enrichedPayment)
                    .build();

            responseList.add(dto);
        }

        return responseList;
    }
}
