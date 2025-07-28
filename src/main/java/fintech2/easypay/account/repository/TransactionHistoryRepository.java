package fintech2.easypay.account.repository;

import fintech2.easypay.account.entity.TransactionHistory;
import fintech2.easypay.common.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
    List<TransactionHistory> findByAccountNumberAndTransactionTypeInOrderByCreatedAtDesc(String accountNumber, List<TransactionType> transactionTypes);
} 