package fpt.org.inblue.controller;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo payment để mua gói apply cho một JD.
     * Giá tiền được lấy tự động từ JD.price.
     * userId được lấy tự động từ JWT token.
     *
     * @param jdId ID của JobDescription cần mua
     * @return PayOS checkout URL
     */
    @PostMapping("/pay")
    public ResponseEntity<String> createPayment(@RequestParam Long jdId) {
        return ResponseEntity.ok(paymentService.createPayment(jdId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable int id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getPayments() {
        return ResponseEntity.ok(paymentService.getPayments());
    }

    @GetMapping("/cancel")
    public ResponseEntity<Payment> cancelPayment(@RequestParam String transactionCode) {
        return ResponseEntity.ok(paymentService.cancelPayment(transactionCode));
    }

    @PostMapping("/webhook")
    public void handlePayOsWebhook(@RequestBody Webhook body) {
        System.out.println("Received PayOS webhook: " + body.toString());
        paymentService.handlePayOsWebhook(body);
    }
}
