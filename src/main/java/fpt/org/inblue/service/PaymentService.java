package fpt.org.inblue.service;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.enums.PaymentPurpose;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface PaymentService {
     String createPayment(long amount, int userId, PaymentPurpose paymentPurpose);
    Payment getPayment(int id);
    List<Payment> getPayments();
    void handlePayOsWebhook(Webhook body);
    Payment cancelPayment(String transactionCode);
}
