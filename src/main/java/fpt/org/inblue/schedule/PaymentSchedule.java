package fpt.org.inblue.schedule;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.dto.payos.PaymentStatusResponse;
import fpt.org.inblue.model.enums.PaymentStatus;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.TransactionRepository;
import fpt.org.inblue.utils.HelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentSchedule {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${payos.client-id}")
    private String clientId;
    @Value("${payos.api-key}")
    private String apiKey;
    @Autowired
    private TransactionRepository transactionRepository;

    @Scheduled(fixedDelay = 300000)
   public void checkPaymentStatus() {
        System.out.println("Checking pending payments at " + LocalDateTime.now());
        LocalDateTime times = LocalDateTime.now().minusMinutes(10);
        List<Payment> payments = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, times);

        for(Payment payment : payments) {
            try {
                System.out.println("Checking payment: " + payment.getId());
                String url = "https://api-merchant.payos.vn/v2/payment-requests/"
                        + payment.getTransactionCode();

                HttpHeaders headers = new HttpHeaders();
                headers.set("x-client-id", clientId);
                headers.set("x-api-key", apiKey);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<PaymentStatusResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        PaymentStatusResponse.class
                );

                if (response.getBody() != null) {
                    String transactionCode = payment.getTransactionCode();
                    String type = HelperUtil.getPrefix(transactionCode);
                    String status = response.getBody().getData().getStatus();
                    if (type.equals("100") && ("CANCELLED".equals(status) || "EXPIRED".equals(status) || "PENDING".equals(status))) {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        System.out.println("Auto-cancelled payment: " + payment.getId());
                    }
                    else if (type.equals("200") && ("CANCELLED".equals(status) || "EXPIRED".equals(status)  || "PENDING".equals(status))) {
                        transactionRepository.deleteByTransactionCode(payment.getTransactionCode());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

