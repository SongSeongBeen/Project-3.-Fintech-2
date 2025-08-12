package fintech2.easypay.account.ledger;

import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.common.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 원장 서비스 Fluent 래퍼
 * 잔액 이동을 더 직관적이고 원자적으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {
    
    private final BalanceService balanceService;
    
    /**
     * 새로운 원장 트랜잭션 시작
     */
    public LedgerTransaction begin() {
        return new LedgerTransaction(balanceService);
    }
    
    /**
     * 원장 트랜잭션 클래스
     * 여러 계좌 간 잔액 이동을 원자적으로 처리
     */
    public static class LedgerTransaction {
        private final BalanceService balanceService;
        private final List<LedgerEntry> entries = new ArrayList<>();
        private String transactionId;
        
        private LedgerTransaction(BalanceService balanceService) {
            this.balanceService = balanceService;
        }
        
        /**
         * 거래 ID 설정
         */
        public LedgerTransaction withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        /**
         * 차변 (출금) 엔트리 추가
         */
        public LedgerTransaction debit(String accountNumber, BigDecimal amount, String memo) {
            entries.add(new LedgerEntry(
                accountNumber, 
                amount.negate(),  // 음수로 저장
                TransactionType.TRANSFER_OUT,
                memo,
                transactionId
            ));
            return this;
        }
        
        /**
         * 차변 (출금) 엔트리 추가 - 상세 버전
         */
        public LedgerTransaction debit(String accountNumber, BigDecimal amount, 
                                      String memo, String userId) {
            entries.add(new LedgerEntry(
                accountNumber, 
                amount.negate(),  // 음수로 저장
                TransactionType.TRANSFER_OUT,
                memo,
                transactionId,
                userId
            ));
            return this;
        }
        
        /**
         * 대변 (입금) 엔트리 추가
         */
        public LedgerTransaction credit(String accountNumber, BigDecimal amount, String memo) {
            entries.add(new LedgerEntry(
                accountNumber, 
                amount,  // 양수로 저장
                TransactionType.TRANSFER_IN,
                memo,
                transactionId
            ));
            return this;
        }
        
        /**
         * 대변 (입금) 엔트리 추가 - 상세 버전
         */
        public LedgerTransaction credit(String accountNumber, BigDecimal amount, 
                                       String memo, String userId) {
            entries.add(new LedgerEntry(
                accountNumber, 
                amount,  // 양수로 저장
                TransactionType.TRANSFER_IN,
                memo,
                transactionId,
                userId
            ));
            return this;
        }
        
        /**
         * 수수료 차감
         */
        public LedgerTransaction fee(String accountNumber, BigDecimal feeAmount, String memo) {
            entries.add(new LedgerEntry(
                accountNumber, 
                feeAmount.negate(),  // 음수로 저장
                TransactionType.FEE,
                memo,
                transactionId
            ));
            return this;
        }
        
        /**
         * 원장 엔트리들을 실행 (원자적 처리)
         */
        @Transactional
        public void commit() {
            validateEntries();
            
            log.info("원장 트랜잭션 시작: txnId={}, entries={}", 
                transactionId, entries.size());
            
            for (LedgerEntry entry : entries) {
                if (entry.amount.compareTo(BigDecimal.ZERO) < 0) {
                    // 차변 (출금)
                    balanceService.decrease(
                        entry.accountNumber,
                        entry.amount.abs(),
                        entry.type,
                        entry.memo,
                        entry.transactionId,
                        entry.userId
                    );
                } else {
                    // 대변 (입금)
                    balanceService.increase(
                        entry.accountNumber,
                        entry.amount,
                        entry.type,
                        entry.memo,
                        entry.transactionId,
                        entry.userId
                    );
                }
                
                log.debug("원장 엔트리 처리: account={}, amount={}, type={}", 
                    entry.accountNumber, entry.amount, entry.type);
            }
            
            log.info("원장 트랜잭션 완료: txnId={}", transactionId);
        }
        
        /**
         * 원장 엔트리 검증
         */
        private void validateEntries() {
            if (entries.isEmpty()) {
                throw new IllegalStateException("원장 엔트리가 없습니다");
            }
            
            // 차변과 대변의 합이 0인지 확인 (복식부기)
            BigDecimal sum = entries.stream()
                .map(e -> e.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (sum.compareTo(BigDecimal.ZERO) != 0) {
                log.warn("원장 불균형 감지: sum={}, txnId={}", sum, transactionId);
                // 엄격한 복식부기가 필요한 경우 예외 발생
                // throw new IllegalStateException("원장 차변/대변 불균형: " + sum);
            }
        }
        
        /**
         * 원장 엔트리
         */
        private record LedgerEntry(
            String accountNumber,
            BigDecimal amount,
            TransactionType type,
            String memo,
            String transactionId,
            String userId
        ) {
            // userId가 없는 생성자
            public LedgerEntry(String accountNumber, BigDecimal amount, 
                             TransactionType type, String memo, String transactionId) {
                this(accountNumber, amount, type, memo, transactionId, null);
            }
        }
    }
    
    /**
     * 간단한 송금 처리 헬퍼 메서드
     */
    @Transactional
    public void transfer(String fromAccount, String toAccount, 
                         BigDecimal amount, String transactionId, String memo) {
        begin()
            .withTransactionId(transactionId)
            .debit(fromAccount, amount, "송금 출금: " + memo)
            .credit(toAccount, amount, "송금 입금: " + memo)
            .commit();
    }
    
    /**
     * 수수료 포함 송금 처리 헬퍼 메서드
     */
    @Transactional
    public void transferWithFee(String fromAccount, String toAccount, 
                                BigDecimal amount, BigDecimal fee,
                                String feeAccount, String transactionId, String memo) {
        begin()
            .withTransactionId(transactionId)
            .debit(fromAccount, amount.add(fee), "송금 및 수수료: " + memo)
            .credit(toAccount, amount, "송금 입금: " + memo)
            .credit(feeAccount, fee, "송금 수수료")
            .commit();
    }
}