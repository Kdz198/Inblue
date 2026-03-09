package fpt.org.inblue.service;

import fpt.org.inblue.model.Transaction;
import fpt.org.inblue.model.dto.request.TransactionRequest;

import java.util.List;

public interface TransactionService {
    String transferIn(long amount, int userId);
    Transaction getTransactionByTransactionCode(String transactionCode);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByUserId(int userId);
    void deleteTransactionByTransactionCode(String transactionCode);
    String transferOut(long amount, int userId);

}
