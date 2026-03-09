package fpt.org.inblue.controller;

import fpt.org.inblue.model.Transaction;
import fpt.org.inblue.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "Tạo giao dịch nạp tiền vào ví, trả về link thanh toán của PayOS để FE redirect người dùng sang trang thanh toán")
    @PostMapping("/transfer-in")
    public ResponseEntity<String> transferIn(@RequestParam long amount, @RequestParam int userId) {
        String paymentLink = transactionService.transferIn(amount, userId);
        return ResponseEntity.ok(paymentLink);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("{transactionCode}")
    public ResponseEntity<Transaction> getTransactionByTransactionCode(@PathVariable String transactionCode) {
        return ResponseEntity.ok(transactionService.getTransactionByTransactionCode(transactionCode));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @Operation(summary = "Nếu FE nhận đc rediect về trang cancelUrl thì gọi API này để xóa transaction đã tạo trước đó")
    @DeleteMapping("/{transactionCode}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionCode) {
        transactionService.deleteTransactionByTransactionCode(transactionCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary ="Dùng cho giao dịch khi mà mua gói từ ví")
    @PostMapping("/transfer-out")
    public ResponseEntity<String> transferOut(@RequestParam long amount, @RequestParam int userId) {
        String result = transactionService.transferOut(amount, userId);
        return ResponseEntity.ok(result);
    }
}
