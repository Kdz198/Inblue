package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.model.Transaction;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.enums.PaymentStatus;
import fpt.org.inblue.repository.PaymentRepository;
import fpt.org.inblue.repository.TransactionRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static fpt.org.inblue.utils.HelperUtil.generateUniqueOrderCode;

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
    @Autowired
    private TransactionRepository transactionRepository;

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
                .description("PAYMENT")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        CreatePaymentLinkResponse paymentLink = payOS.paymentRequests().create(request);
        return paymentLink.getCheckoutUrl();
    }


    @Override
    public void handlePayOsWebhook(Webhook body) {
        try {
            WebhookData webhookData = payOS.webhooks().verify(body);
            String type = webhookData.getDescription().split(" ")[1];
            String transactionCode = String.valueOf(webhookData.getOrderCode());
            if(type.equals("PAYMENT")){
                Payment payment = paymentRepository.findByTransactionCode(transactionCode);
                if (webhookData.getDesc().equals("success")) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                }
                paymentRepository.save(payment);
            }
            else{
                Transaction transaction = transactionRepository.findByTransactionCode(transactionCode);
                User user = transaction.getUser();
                transaction.setDescription("Nạp tiền vào ví Inblue");
                long currentBalance = transaction.getUser().getWalletBalance();
                currentBalance += transaction.getAmount();
                transaction.setCurrentBalance(currentBalance);
                user.setWalletBalance(currentBalance);
                transactionRepository.save(transaction);
                userRepository.save(user);
            }

        } catch (Exception e) {
            System.err.println("Error processing PayOS webhook: " + e.getMessage());
        }
    }

    @Override
    public Payment cancelPayment(String transactionCode) {
        Payment payment = paymentRepository.findByTransactionCode(transactionCode);
        if (payment == null) {
            throw new CustomException("Payment not found with transaction code: " + transactionCode, HttpStatus.NOT_FOUND);
        }
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }
}
