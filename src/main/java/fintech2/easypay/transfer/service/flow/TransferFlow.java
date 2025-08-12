package fintech2.easypay.transfer.service.flow;

import lombok.extern.slf4j.Slf4j;

import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.NotificationService;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.common.enums.AuditEventType;
import fintech2.easypay.common.enums.TransactionType;
import fintech2.easypay.transfer.dto.TransferRequest;
import fintech2.easypay.transfer.dto.TransferResponse;
import fintech2.easypay.transfer.entity.Transfer;
import fintech2.easypay.transfer.entity.TransferStatus;
import fintech2.easypay.transfer.client.*;
import fintech2.easypay.transfer.repository.TransferRepository;
import fintech2.easypay.transfer.service.TransferValidationService;
import fintech2.easypay.transfer.service.flow.context.TransferContext;

/**
 * 송금 플로우 Fluent API
 * 송금 처리 과정을 체이닝 방식으로 구성
 */
@Slf4j
public class TransferFlow {
    
    private final TransferValidationService validationService;
    private final TransferRepository transferRepository;
    private final BalanceService balanceService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final BankingApiService bankingApiService;
    
    private TransferContext ctx;
    
    /**
     * 생성자 (일반 클래스로 변경 - 서비스에서 매번 생성)
     */
    public TransferFlow(
            TransferValidationService validationService,
            TransferRepository transferRepository,
            BalanceService balanceService,
            AuditLogService auditLogService,
            NotificationService notificationService,
            BankingApiService bankingApiService) {
        this.validationService = validationService;
        this.transferRepository = transferRepository;
        this.balanceService = balanceService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.bankingApiService = bankingApiService;
    }
    
    /**
     * 정적 팩토리 메서드로 플로우 시작
     */
    public static TransferFlow start(
            TransferValidationService validationService,
            TransferRepository transferRepository,
            BalanceService balanceService,
            AuditLogService auditLogService,
            NotificationService notificationService,
            BankingApiService bankingApiService) {
        
        return new TransferFlow(
            validationService, 
            transferRepository, 
            balanceService,
            auditLogService, 
            notificationService, 
            bankingApiService
        );
    }
    
    /**
     * 초기화: 참여자 검증 및 계좌 락 획득
     */
    public TransferFlow init(String senderPhone, TransferRequest req, String txnId) {
        log.debug("송금 플로우 초기화: txnId={}, sender={}", txnId, senderPhone);
        
        // 참여자 검증
        var participants = validationService.validateAndGetParticipants(
            senderPhone, 
            req.getReceiverAccountNumber()
        );
        
        // 송금자 계좌 조회
        var senderAccount = validationService.getSenderAccount(
            participants.sender(), 
            req.getSenderAccountNumber()
        );
        
        // 데드락 방지를 위한 순서화된 락 획득
        var locked = validationService.acquireAccountLocks(
            senderAccount, 
            participants.receiverAccount()
        );
        
        // 컨텍스트 생성
        this.ctx = TransferContext.builder()
                .senderPhone(senderPhone)
                .transactionId(txnId)
                .sender(participants.sender())
                .receiver(participants.receiver())
                .senderAccount(locked.senderAccount())
                .receiverAccount(locked.receiverAccount())
                .amount(req.getAmount())
                .memo(req.getMemo())
                .build();
        
        log.info("송금 플로우 초기화 완료: {} -> {}", 
            ctx.getSenderAccount().getAccountNumber(),
            ctx.getReceiverAccount().getAccountNumber());
        
        return this;
    }
    
    /**
     * 잔액 충분성 확인
     */
    public TransferFlow ensureBalance() {
        log.debug("잔액 확인: account={}, amount={}", 
            ctx.getSenderAccount().getAccountNumber(), ctx.getAmount());
        
        if (!balanceService.hasSufficientBalance(
                ctx.getSenderAccount().getAccountNumber(), 
                ctx.getAmount())) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                "잔액이 부족합니다. 현재 잔액을 확인해주세요.");
        }
        
        log.debug("잔액 확인 완료: 충분");
        return this;
    }
    
    /**
     * 송금 기록을 PENDING 상태로 저장
     */
    public TransferFlow persistPending() {
        log.debug("송금 기록 생성: txnId={}", ctx.getTransactionId());
        
        Transfer transfer = Transfer.builder()
                .transactionId(ctx.getTransactionId())
                .sender(ctx.getSender())
                .senderAccountNumber(ctx.getSenderAccount().getAccountNumber())
                .receiver(ctx.getReceiver())
                .receiverAccountNumber(ctx.getReceiverAccount().getAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .status(TransferStatus.REQUESTED)
                .build()
                .markAsPending();  // Fluent 체이닝
        
        transfer = transferRepository.save(transfer);
        
        // PROCESSING 상태로 전환
        transfer.markAsProcessing();
        transfer = transferRepository.save(transfer);
        
        this.ctx = ctx.withTransfer(transfer);
        
        log.info("송금 기록 저장 완료: id={}, status={}", 
            transfer.getId(), transfer.getStatus());
        
        return this;
    }
    
    /**
     * 외부 뱅킹 API 호출
     */
    public TransferFlow callExternal() {
        log.info("외부 뱅킹 API 호출 시작: txnId={}", ctx.getTransactionId());
        
        BankingApiRequest apiReq = BankingApiRequest.builder()
                .transactionId(ctx.getTransactionId())
                .senderAccountNumber(ctx.getSenderAccount().getAccountNumber())
                .senderBankCode("999")  // EASYPAY 코드
                .receiverAccountNumber(ctx.getReceiverAccount().getAccountNumber())
                .receiverBankCode("999")  // EASYPAY 코드
                .amount(ctx.getAmount())
                .currency("KRW")
                .memo(ctx.getMemo())
                .build();
        
        BankingApiResponse res = bankingApiService.processTransfer(apiReq);
        this.ctx = ctx.withApiResponse(res);
        
        log.info("외부 뱅킹 API 응답 수신: status={}", res.getStatus());
        
        return this;
    }
    
    /**
     * 원장 반영 및 상태 확정
     */
    public TransferFlow applyLedgerAndFinalize() {
        var res = ctx.getApiResponse();
        var transfer = ctx.getTransfer();
        
        log.debug("원장 반영 시작: txnId={}, apiStatus={}", 
            ctx.getTransactionId(), res.getStatus());
        
        switch (res.getStatus()) {
            case SUCCESS -> {
                // 잔액 이동
                balanceService.decrease(
                    ctx.getSenderAccount().getAccountNumber(), 
                    ctx.getAmount(),
                    TransactionType.TRANSFER_OUT, 
                    "송금 출금: " + ctx.getMemo(), 
                    ctx.getTransactionId(), 
                    ctx.getSender().getId().toString()
                );
                
                balanceService.increase(
                    ctx.getReceiverAccount().getAccountNumber(), 
                    ctx.getAmount(),
                    TransactionType.TRANSFER_IN, 
                    "송금 입금: " + ctx.getMemo(), 
                    ctx.getTransactionId(), 
                    ctx.getReceiver().getId().toString()
                );
                
                // 상태 업데이트
                transfer.markAsCompleted(res.getBankTransactionId());
                log.info("송금 완료: txnId={}", ctx.getTransactionId());
            }
            case TIMEOUT -> {
                transfer.markAsTimeout("외부 API 타임아웃: " + res.getErrorMessage());
                log.warn("송금 타임아웃: txnId={}", ctx.getTransactionId());
            }
            case UNKNOWN -> {
                transfer.markAsUnknown("외부 API 응답 상태 확인 불가");
                log.warn("송금 상태 불명: txnId={}", ctx.getTransactionId());
            }
            case PENDING -> {
                // PROCESSING 상태 유지
                log.info("송금 처리 중: txnId={}", ctx.getTransactionId());
            }
            default -> {
                String errorMsg = String.format("외부 API 오류: %s - %s",
                    res.getStatus().getDescription(), 
                    res.getErrorMessage());
                transfer.markAsFailed(errorMsg);
                log.error("송금 실패: txnId={}, reason={}", ctx.getTransactionId(), errorMsg);
                throw new BusinessException(ErrorCode.TRANSACTION_FAILED, errorMsg);
            }
        }
        
        transferRepository.save(transfer);
        return this;
    }
    
    /**
     * 감사 로그 및 알림 처리
     */
    public TransferFlow auditAndNotify() {
        var transfer = ctx.getTransfer();
        
        // 성공 또는 처리중 상태인 경우만 성공 로그
        if (transfer.getStatus() == TransferStatus.COMPLETED || 
            transfer.getStatus() == TransferStatus.PROCESSING) {
            
            // 감사 로그
            auditLogService.logSuccess(
                ctx.getSender().getId(),
                ctx.getSenderPhone(),
                AuditEventType.TRANSFER_SUCCESS,
                String.format("송금 완료: %s -> %s (%s원)", 
                    ctx.getSenderAccount().getAccountNumber(),
                    ctx.getReceiverAccount().getAccountNumber(), 
                    ctx.getAmount()),
                null, null,
                String.format("amount: %s, memo: %s", ctx.getAmount(), ctx.getMemo()),
                String.format("transactionId: %s", ctx.getTransactionId())
            );
            
            // 알림 전송
            notificationService.sendTransferActivityNotification(
                ctx.getSender().getId(), 
                ctx.getSenderPhone(),
                String.format("%s원이 %s로 송금되었습니다.", 
                    ctx.getAmount(), 
                    ctx.getReceiverAccount().getAccountNumber())
            );
            
            notificationService.sendTransferActivityNotification(
                ctx.getReceiver().getId(), 
                ctx.getReceiver().getPhoneNumber(),
                String.format("%s원이 %s로부터 입금되었습니다.", 
                    ctx.getAmount(), 
                    ctx.getSenderAccount().getAccountNumber())
            );
        }
        
        return this;
    }
    
    /**
     * 최종 응답 생성
     */
    public TransferResponse toResponse() {
        return TransferResponse.from(ctx.getTransfer());
    }
    
    /**
     * 에러 처리용 메서드
     */
    public TransferFlow onError(Exception e) {
        log.error("송금 플로우 에러: txnId={}, error={}", 
            ctx != null ? ctx.getTransactionId() : "N/A", 
            e.getMessage(), e);
        
        if (ctx != null && ctx.getTransfer() != null) {
            ctx.getTransfer().markAsFailed(e.getMessage());
            transferRepository.save(ctx.getTransfer());
        }
        
        return this;
    }
}