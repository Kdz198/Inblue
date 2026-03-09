package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.TransactionMapper;
import fpt.org.inblue.model.Transaction;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.request.TransactionRequest;
import fpt.org.inblue.repository.TransactionRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

import java.util.List;

import static fpt.org.inblue.utils.HelperUtil.generateUniqueOrderCode;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PayOS payOS;
    @Value("${payos.return-url}")
    private String returnUrl;
    @Value("${payos.cancel-url}")
    private String cancelUrl;


    @Override
    public String transferIn(long amount, int userId) {
        long transactionCode = generateUniqueOrderCode();
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setTransactionCode(String.valueOf(transactionCode));
        transaction.setTransactionType(true);
        transactionRepository.save(transaction);
       return createPayOSPayment(amount, transactionCode);
    }

    private String createPayOSPayment(long amount, long transactionCode) {
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .amount(amount)
                .orderCode(transactionCode)
                .description("WALLET")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        var paymentLink = payOS.paymentRequests().create(request);
        return paymentLink.getCheckoutUrl();
    }

    @Override
    public Transaction getTransactionByTransactionCode(String transactionCode) {
        return transactionRepository.findByTransactionCode(transactionCode);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getTransactionsByUserId(int userId) {
        return transactionRepository.findAllByUser_Id(userId);
    }

    @Override
    public void deleteTransactionByTransactionCode(String transactionCode) {
        transactionRepository.deleteByTransactionCode(transactionCode);
    }

    @Override
    public String transferOut(long amount, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        if(user.getWalletBalance() < amount){
            throw new CustomException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }
        long currentBalance = user.getWalletBalance()-amount;
        user.setWalletBalance(currentBalance);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setTransactionType(false);
        transaction.setTransactionCode(String.valueOf(generateUniqueOrderCode()));
        transaction.setDescription("Rút tiền từ ví");
        transaction.setCurrentBalance(currentBalance);
        transactionRepository.save(transaction);
        userRepository.save(user);
        return "Transfer out successful. Current balance: " + currentBalance;
    }
}
