package fintech2.easypay.transfer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.common.enums.AuditEventType;
import fintech2.easypay.common.enums.TransactionType;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.NotificationService;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.transfer.dto.RecentTransferResponse;
import fintech2.easypay.transfer.dto.TransferRequest;
import fintech2.easypay.transfer.dto.TransferResponse;
import fintech2.easypay.transfer.entity.Transfer;
import fintech2.easypay.transfer.entity.TransferStatus;
import fintech2.easypay.transfer.client.BankingApiRequest;
import fintech2.easypay.transfer.client.BankingApiResponse;
import fintech2.easypay.transfer.client.BankingApiService;
import fintech2.easypay.transfer.client.BankingApiStatus;
import fintech2.easypay.transfer.repository.TransferRepository;
import fintech2.easypay.transfer.enums.BankCode;
import fintech2.easypay.transfer.service.flow.TransferFlow;
import fintech2.easypay.transfer.support.id.TransactionIdGenerator;
import fintech2.easypay.account.ledger.LedgerService;
import org.springframework.context.ApplicationContext;

/**
 * 송금 서비스
 * 사용자 간 송금 처리 및 거래 내역 관리
 * Fluent API를 활용한 깔끔한 송금 처리 플로우
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransferService {
    
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final BankingApiService bankingApiService;
    private final ApplicationContext applicationContext;
    private final TransferValidationService validationService;
    private final TransactionIdGenerator transactionIdGenerator;
    private final LedgerService ledgerService;
    
    /**
     * 사용자 간 송금 처리 (Fluent API 사용)
     * @param senderPhoneNumber 송금자 휴대폰 번호
     * @param request 송금 요청 정보
     * @return 송금 처리 결과
     * @throws BusinessException 송금 처리 중 오류 발생 시
     */
    @Transactional
    public TransferResponse transfer(String senderPhoneNumber, TransferRequest request) {
        // 거래 ID 생성
        String transactionId = transactionIdGenerator.generate();
        
        try {
            // Fluent API를 사용한 송금 처리
            return TransferFlow.start(
                    validationService, transferRepository, balanceService,
                    auditLogService, notificationService, bankingApiService)
                .init(senderPhoneNumber, request, transactionId)
                .ensureBalance()
                .persistPending()
                .callExternal()
                .applyLedgerAndFinalize()
                .auditAndNotify()
                .toResponse();
                
        } catch (Exception e) {
            // 실패 감사 로그
            auditLogService.logFailure(
                null, // init 이전이면 senderId가 null일 수 있음
                senderPhoneNumber,
                AuditEventType.TRANSFER_FAILED,
                "송금 실패: " + e.getMessage(),
                null, null,
                String.format("amount: %s, memo: %s", request.getAmount(), request.getMemo()),
                e.getMessage()
            );
            
            log.error("송금 실패: txnId={}, sender={}, error={}", 
                transactionId, senderPhoneNumber, e.getMessage(), e);
            
            throw new BusinessException(ErrorCode.TRANSACTION_FAILED, e.getMessage());
        }
    }
    
    /**
     * 거래 조회
     */
    public TransferResponse getTransfer(String transactionId) {
        Transfer transfer = transferRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        
        return TransferResponse.from(transfer);
    }
    
    /**
     * 거래 내역 조회 (전체)
     */
    public Page<TransferResponse> getTransferHistory(String phoneNumber, Pageable pageable) {
        Page<Transfer> transfers = transferRepository.findByPhoneNumberOrderByCreatedAtDesc(phoneNumber, pageable);
        return transfers.map(TransferResponse::from);
    }
    
    /**
     * 송금 내역 조회
     */
    public Page<TransferResponse> getSentTransfers(String phoneNumber, Pageable pageable) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Page<Transfer> transfers = transferRepository.findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable);
        return transfers.map(TransferResponse::from);
    }
    
    /**
     * 입금 내역 조회
     */
    public Page<TransferResponse> getReceivedTransfers(String phoneNumber, Pageable pageable) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Page<Transfer> transfers = transferRepository.findByReceiverIdOrderByCreatedAtDesc(user.getId(), pageable);
        return transfers.map(TransferResponse::from);
    }
    
    /**
     * 최근 송금한 사람들의 목록 조회 (중복 제거)
     * 가장 최근에 송금한 순서대로 정렬
     */
    public Page<RecentTransferResponse> getRecentTransfers(String phoneNumber, Pageable pageable) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 성공한 송금만 조회하고, 수신자별로 가장 최근 거래만 가져옴
        Page<Transfer> transfers = transferRepository.findRecentDistinctReceivers(user.getId(), pageable);
        return transfers.map(RecentTransferResponse::from);
    }
    
    /**
     * LedgerService를 활용한 간편 송금 (내부 테스트용)
     */
    @Transactional
    public void simpleTransfer(String fromAccount, String toAccount, 
                               BigDecimal amount, String memo) {
        String transactionId = transactionIdGenerator.generateWithPrefix("SIMPLE");
        
        // Fluent 원장 처리
        ledgerService.begin()
            .withTransactionId(transactionId)
            .debit(fromAccount, amount, "간편 송금 출금: " + memo)
            .credit(toAccount, amount, "간편 송금 입금: " + memo)
            .commit();
            
        log.info("간편 송금 완료: {} -> {}, 금액: {}원", fromAccount, toAccount, amount);
    }
}