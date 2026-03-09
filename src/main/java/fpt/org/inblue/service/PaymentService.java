package fpt.org.inblue.service;

import fpt.org.inblue.model.Payment;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface PaymentService {
     String createPayment(long amount, int userId);
    Payment getPayment(int id);
    List<Payment> getPayments();

    void handlePayOsWebhook(Webhook body);
}
