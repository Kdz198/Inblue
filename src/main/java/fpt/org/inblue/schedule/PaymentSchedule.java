package fpt.org.inblue.schedule;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.enums.PaymentStatus;
import fpt.org.inblue.repository.PaymentRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;

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
    @Value("${payos.checksum-key}")

    @Scheduled(fixedDelay = 600000)
   public void checkPaymentStatus() {
        System.out.println("Checking pending payments at " + LocalDateTime.now());
        LocalDateTime times = LocalDateTime.now().minusMinutes(10);
        List<Payment> payments = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, times);

        for(Payment payment : payments) {
            try {
                System.out.println("Checking payment: " + payment.getId());
                String url = "https://api-merchant.payos.vn/v2/payment-requests/"
                        + payment.getId();

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
                    String status = response.getBody().getData().getStatus();
                    System.out.println("Payment " + payment.getId() + " status: " + status);

                    if ("CANCELLED".equals(status)) {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        System.out.println("✓ Auto-cancelled payment: " + payment.getId());
                    }
                    else if ("EXPIRED".equals(status)) {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        System.out.println("✓ Expired payment: " + payment.getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
@Data
class PaymentStatusResponse {
    private String code;
    private String desc;
    private PaymentStatusData data;
}

@Data
class PaymentStatusData {
    private String status;
    private Long orderCode;
    private Integer amount;
}
