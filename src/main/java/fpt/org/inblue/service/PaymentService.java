package fpt.org.inblue.service;

import fpt.org.inblue.model.Payment;
import java.util.List;
import vn.payos.model.webhooks.Webhook;

public interface PaymentService {

    /**
     * Tạo payment để mua gói apply cho một JD.
     * Giá tiền được lấy tự động từ field price của JobDescription.
     * userId được lấy tự động từ JWT token.
     *
     * @param jdId ID của JobDescription
     * @return PayOS checkout URL
     */
    String createPayment(Long jdId);

    /**
     * Tạo payment cho mentor session (có amount riêng, không liên quan đến JD).
     */
    String createSessionPayment(long amount);

    Payment getPayment(int id);

    List<Payment> getPayments();

    void handlePayOsWebhook(Webhook body);

    Payment cancelPayment(String transactionCode);
}
