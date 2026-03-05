package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.enums.PaymentStatus;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PayOS payOS;
    @Value("${payos.return-url}")
    private String returnUrl;
    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPayment(int id) {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id: " + id);
        } else {
            return payment;
        }
    }

    @Override
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public String createPayOSPayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        paymentRepository.save(payment);
        long orderCode = payment.getId();
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .amount(payment.getAmount())
                .orderCode(orderCode)
                .description("Thanh toán đơn hàng")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        var paymentLink = payOS.paymentRequests().create(request);
        return paymentLink.getCheckoutUrl();
    }


    @Override
    public void handlePayOsWebhook(Webhook body) {
        try {
            WebhookData webhookData = payOS.webhooks().verify(body);
            Payment payment = paymentRepository.findById(Math.toIntExact(webhookData.getOrderCode()));
            if (webhookData.getCode().equals("00")) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else if(webhookData.getCode().equals("02")) {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println("Error processing PayOS webhook: " + e.getMessage());
        }

    }
}
