package fpt.org.inblue.service;

import fpt.org.inblue.model.Payment;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment getPayment(int id);
    List<Payment> getPayments();
    String createPayOSPayment(int paymentId);
    void handlePayOsWebhook(Webhook body);
}
