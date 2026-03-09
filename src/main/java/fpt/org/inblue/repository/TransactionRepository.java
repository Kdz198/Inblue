package fpt.org.inblue.repository;

import fpt.org.inblue.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllByUser_Id(int userId);

    Transaction findByTransactionCode(String transactionCode);

    void deleteByTransactionCode(String transactionCode);
}
