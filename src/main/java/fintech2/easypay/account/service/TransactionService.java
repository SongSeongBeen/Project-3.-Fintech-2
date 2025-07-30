package fintech2.easypay.account.service;

import fintech2.easypay.account.entity.TransactionHistory;
import fintech2.easypay.account.repository.TransactionHistoryRepository;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.common.enums.TransactionStatus;
import fintech2.easypay.common.enums.TransactionType;
import fintech2.easypay.common.exception.AccountNotFoundException;
import fintech2.easypay.common.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 계좌 이체 서비스
 * 계좌 간 송금 기능을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService { // TransferService -> TransactionService

    private final BalanceService balanceService;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AuditLogService auditLogService;

    /**
     * 계좌 간 송금
     */
    @Transactional
    public ResponseEntity<?> transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description, String userId) {
        String transferId = UUID.randomUUID().toString();
        
        try {
            // 출금 (송금자 계좌에서 차감)
            BalanceService.BalanceChangeResult debitResult = balanceService.decrease(
                fromAccountNumber, 
                amount, 
                TransactionType.TRANSFER_OUT, 
                "송금: " + description, 
                transferId, 
                userId
            );

            // 입금 (수취자 계좌에 추가)
            BalanceService.BalanceChangeResult creditResult = balanceService.increase(
                toAccountNumber, 
                amount, 
                TransactionType.TRANSFER_IN, 
                "입금: " + description, 
                transferId, 
                userId
            );

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("transferId", transferId);
            response.put("fromAccount", fromAccountNumber);
            response.put("toAccount", toAccountNumber);
            response.put("amount", amount);
            response.put("description", description);
            response.put("fromBalanceAfter", debitResult.getBalanceAfter());
            response.put("toBalanceAfter", creditResult.getBalanceAfter());
            response.put("status", "COMPLETED");
            response.put("timestamp", LocalDateTime.now());

            // 감사 로그 기록
            auditLogService.logSuccess(
                userId != null ? Long.parseLong(userId) : null,
                "TRANSFER", 
                "ACCOUNT", 
                transferId, 
                String.format("송금: %s -> %s", fromAccountNumber, toAccountNumber),
                response.toString()
            );

            log.info("송금 완료: {} -> {}, 금액: {}, 송금ID: {}", 
                sanitizeLogMessage(fromAccountNumber), 
                sanitizeLogMessage(toAccountNumber), 
                amount, 
                sanitizeLogMessage(transferId));
            return ResponseEntity.ok(response);

        } catch (AccountNotFoundException e) {
            auditLogService.logError("TRANSFER", "ACCOUNT", transferId, "계좌를 찾을 수 없음: " + e.getMessage(), e);
            throw e;
        } catch (InsufficientBalanceException e) {
            auditLogService.logError("TRANSFER", "ACCOUNT", transferId, "잔액 부족: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("송금 중 오류 발생: {}", sanitizeLogMessage(e.getMessage()), e);
            auditLogService.logError("TRANSFER", "ACCOUNT", transferId, "송금 실패: " + e.getMessage(), e);
            throw new RuntimeException("송금 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 송금 내역 조회
     */
    public ResponseEntity<?> getTransferHistory(String accountNumber) {
        try {
            // 송금 내역 조회 (송금/입금 모두 포함)
            var transfers = transactionHistoryRepository.findByAccountNumberAndTransactionTypeInOrderByCreatedAtDesc(
                accountNumber, 
                java.util.List.of(TransactionType.TRANSFER_IN, TransactionType.TRANSFER_OUT)
            );

            Map<String, Object> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("transfers", transfers);
            response.put("totalCount", transfers.size());

            auditLogService.logSuccess("TRANSFER_HISTORY", "ACCOUNT", accountNumber, "송금 내역 조회 성공", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("송금 내역 조회 중 오류 발생: {}", sanitizeLogMessage(e.getMessage()), e);
            auditLogService.logError("TRANSFER_HISTORY", "ACCOUNT", accountNumber, "송금 내역 조회 실패", e);
            throw new RuntimeException("송금 내역 조회 중 오류가 발생했습니다", e);
        }
    }
    
    /**
     * 로그 메시지에서 CRLF 문자를 제거하여 로그 주입 취약점을 방지합니다.
     */
    private String sanitizeLogMessage(String message) {
        if (message == null) {
            return null;
        }
        // CRLF 문자 제거
        return message.replaceAll("[\r\n]", " ");
    }
} 