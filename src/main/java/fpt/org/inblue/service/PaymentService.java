package fpt.org.inblue.service;

import fpt.org.inblue.enums.PaymentPurpose;
import fpt.org.inblue.model.Payment;
import java.util.List;
import vn.payos.model.webhooks.Webhook;

public interface PaymentService {
    String createPayment(long amount, int userId, PaymentPurpose paymentPurpose);

    Payment getPayment(int id);

    List<Payment> getPayments();

    void handlePayOsWebhook(Webhook body);

    Payment cancelPayment(String transactionCode);
}
