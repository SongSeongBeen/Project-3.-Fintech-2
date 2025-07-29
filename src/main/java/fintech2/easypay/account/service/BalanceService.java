package fintech2.easypay.account.service;

import fintech2.easypay.account.entity.AccountBalance;
import fintech2.easypay.account.entity.TransactionHistory;
import fintech2.easypay.account.repository.AccountBalanceRepository;
import fintech2.easypay.account.repository.TransactionHistoryRepository;
import fintech2.easypay.audit.service.AlarmService;
import fintech2.easypay.common.enums.TransactionStatus;
import fintech2.easypay.common.enums.TransactionType;
import fintech2.easypay.common.exception.AccountNotFoundException;
import fintech2.easypay.common.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 중앙화된 잔액 관리 서비스
 * 모든 잔액 변경 작업을 이 서비스를 통해 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AlarmService alarmService;

    /**
     * 계좌 잔액 조회
     */
    public BigDecimal getBalance(String accountNumber) {
        AccountBalance balance = accountBalanceRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다: " + accountNumber));
        
        return balance.getBalance();
    }

    /**
     * 잔액 증가 (입금)
     */
    @Transactional
    public BalanceChangeResult increase(String accountNumber, BigDecimal amount, TransactionType transactionType, 
                                     String description, String referenceId, String userId) {
        return changeBalance(accountNumber, amount, transactionType, description, referenceId, userId, true);
    }

    /**
     * 잔액 감소 (출금)
     */
    @Transactional
    public BalanceChangeResult decrease(String accountNumber, BigDecimal amount, TransactionType transactionType, 
                                     String description, String referenceId, String userId) {
        return changeBalance(accountNumber, amount.negate(), transactionType, description, referenceId, userId, false);
    }

    /**
     * 잔액 변경 공통 로직
     */
    private BalanceChangeResult changeBalance(String accountNumber, BigDecimal changeAmount, TransactionType transactionType,
                                           String description, String referenceId, String userId, boolean isIncrease) {
        
        // 계좌 잔액 조회 또는 생성
        AccountBalance balance = accountBalanceRepository.findByAccountNumber(accountNumber)
                .orElseGet(() -> {
                    AccountBalance newBalance = new AccountBalance();
                    newBalance.setAccountNumber(accountNumber);
                    newBalance.setBalance(BigDecimal.ZERO);
                    return accountBalanceRepository.save(newBalance);
                });

        BigDecimal balanceBefore = balance.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(changeAmount);

        // 출금인 경우 잔액 확인
        if (!isIncrease && balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            // 잔액 부족 알림 발송
            String currentBalanceStr = balanceBefore.toString();
            String requiredAmountStr = changeAmount.abs().toString();
            alarmService.sendInsufficientBalanceAlert(accountNumber, userId, currentBalanceStr, requiredAmountStr);
            
            throw new InsufficientBalanceException("잔액이 부족합니다. 현재 잔액: " + balanceBefore);
        }

        // 잔액 업데이트
        balance.setBalance(balanceAfter);
        accountBalanceRepository.save(balance);

        // 거래 내역 기록
        String finalReferenceId = referenceId != null ? referenceId : UUID.randomUUID().toString();
        TransactionHistory transaction = TransactionHistory.builder()
                .accountNumber(accountNumber)
                .transactionType(transactionType)
                .amount(isIncrease ? changeAmount : changeAmount.abs())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(finalReferenceId)
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        transactionHistoryRepository.save(transaction);

        // 잔액 변동 알림 발송
        String changeType = isIncrease ? "증가" : "감소";
        String amountStr = changeAmount.abs().toString();
        String balanceAfterStr = balanceAfter.toString();
        alarmService.sendBalanceChangeAlert(accountNumber, userId, changeType, amountStr, balanceAfterStr);

        log.info("잔액 변경 완료: 계좌={}, 변경금액={}, 잔액={}->{}, 거래유형={}", 
                accountNumber, changeAmount, balanceBefore, balanceAfter, transactionType);

        return new BalanceChangeResult(accountNumber, balanceBefore, balanceAfter, 
                                    isIncrease ? changeAmount : changeAmount.abs(), transactionType, finalReferenceId);
    }

    /**
     * 잔액 충분 여부 확인
     */
    public boolean hasSufficientBalance(String accountNumber, BigDecimal amount) {
        try {
            BigDecimal currentBalance = getBalance(accountNumber);
            return currentBalance.compareTo(amount) >= 0;
        } catch (AccountNotFoundException e) {
            return false;
        }
    }

    /**
     * 잔액 변경 결과를 담는 내부 클래스
     */
    public static class BalanceChangeResult {
        private final String accountNumber;
        private final BigDecimal balanceBefore;
        private final BigDecimal balanceAfter;
        private final BigDecimal changeAmount;
        private final TransactionType transactionType;
        private final String referenceId;

        public BalanceChangeResult(String accountNumber, BigDecimal balanceBefore, BigDecimal balanceAfter, 
                                BigDecimal changeAmount, TransactionType transactionType, String referenceId) {
            this.accountNumber = accountNumber;
            this.balanceBefore = balanceBefore;
            this.balanceAfter = balanceAfter;
            this.changeAmount = changeAmount;
            this.transactionType = transactionType;
            this.referenceId = referenceId;
        }

        // Getters
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getBalanceBefore() { return balanceBefore; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public BigDecimal getChangeAmount() { return changeAmount; }
        public TransactionType getTransactionType() { return transactionType; }
        public String getReferenceId() { return referenceId; }
    }
} 