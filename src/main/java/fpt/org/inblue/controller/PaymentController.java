package fpt.org.inblue.controller;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.enums.PaymentPurpose;
import fpt.org.inblue.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;


    @PostMapping("/pay")
    public ResponseEntity<String> createPayment(@RequestParam long amount,@RequestParam int userId, @RequestParam PaymentPurpose paymentPurpose) {
        return ResponseEntity.ok(paymentService.createPayment(amount,userId, paymentPurpose));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable int id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(payment);
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
    public void handlePayOsWebhook(@RequestBody Webhook body){
        System.out.println("Received PayOS webhook: " + body.toString());
        paymentService.handlePayOsWebhook(body);
    }

}
