package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.enums.PaymentStatus;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
    @Autowired
    private UserRepository userRepository;

    @Override
    public String createPayment(long amount, int userId) {
        long transactionCode =generateUniqueOrderCode();
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setUser(user);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionCode(String.valueOf(transactionCode));
        paymentRepository.save(payment);
        return createPayOSPayment(amount, transactionCode);
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

    public String createPayOSPayment(long amount, long transactionCode) {
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .amount(amount)
                .orderCode(transactionCode)
                .description("Thanh toán đơn hàng")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        var paymentLink = payOS.paymentRequests().create(request);
        return paymentLink.getCheckoutUrl();
    }

    public long generateUniqueOrderCode() {
        long timestamp = System.currentTimeMillis() % 1000000000L; // Lấy 9 số cuối của timestamp
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999); // Thêm 3 số ngẫu nhiên
        return Long.parseLong(timestamp + "" + randomSuffix);
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
