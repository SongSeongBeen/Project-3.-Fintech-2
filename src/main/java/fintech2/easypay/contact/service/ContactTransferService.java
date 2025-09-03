package fintech2.easypay.contact.service;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.account.service.UserAccountService;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.common.enums.AuditEventType;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.contact.dto.ContactTransferRequest;
import fintech2.easypay.contact.dto.ContactTransferResponse;
import fintech2.easypay.contact.dto.PendingTransferResponse;
import fintech2.easypay.contact.entity.Contact;
import fintech2.easypay.contact.entity.PendingContactTransfer;
import fintech2.easypay.contact.entity.PendingTransferStatus;
import fintech2.easypay.contact.external.PhoneVerificationResult;
import fintech2.easypay.contact.external.PhoneVerificationService;
import fintech2.easypay.contact.external.SmsService;
import fintech2.easypay.contact.repository.ContactRepository;
import fintech2.easypay.contact.repository.PendingContactTransferRepository;
import fintech2.easypay.transfer.dto.TransferRequest;
import fintech2.easypay.transfer.dto.TransferResponse;
import fintech2.easypay.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactTransferService {
    
    private final ContactRepository contactRepository;
    private final PendingContactTransferRepository pendingTransferRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserAccountService userAccountService;
    private final BalanceService balanceService;
    private final TransferService transferService;
    private final PhoneVerificationService phoneVerificationService;
    private final SmsService smsService;
    private final AuditLogService auditLogService;
    
    @Transactional
    public ContactTransferResponse transferToContact(String senderPhoneNumber, 
                                                     ContactTransferRequest request) {
        // 1. 송금자 조회
        User sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 2. 송금자 계좌 조회
        Account senderAccount = getSenderAccount(sender, request.getSenderAccountNumber());
        
        // 3. 잔액 확인
        if (!balanceService.hasSufficientBalance(senderAccount.getAccountNumber(), request.getAmount())) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        // 4. 연락처 정보 조회 또는 생성
        Contact contact = contactRepository
                .findByOwnerIdAndContactPhoneNumber(sender.getId(), request.getReceiverPhoneNumber())
                .orElseGet(() -> createNewContact(sender, request));
        
        // 5. 수신자가 EasyPay 회원인지 확인
        User receiver = userRepository.findByPhoneNumber(request.getReceiverPhoneNumber())
                .orElse(null);
        
        if (receiver != null) {
            // 5-1. 이미 회원인 경우 - 일반 송금 처리
            return processRegisteredUserTransfer(sender, receiver, request);
        } else {
            // 5-2. 비회원인 경우 - 대기 송금 처리
            return processUnregisteredUserTransfer(sender, senderAccount, contact, request);
        }
    }
    
    private ContactTransferResponse processRegisteredUserTransfer(User sender, User receiver, 
                                                                  ContactTransferRequest request) {
        // 일반 송금 처리
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountNumber(getReceiverAccount(receiver).getAccountNumber());
        transferRequest.setAmount(request.getAmount());
        transferRequest.setMemo(request.getMemo());
        transferRequest.setSenderAccountNumber(request.getSenderAccountNumber());
        
        TransferResponse response = transferService.transfer(sender.getPhoneNumber(), transferRequest);
        
        return ContactTransferResponse.builder()
                .transactionId(response.getTransactionId())
                .status("COMPLETED")
                .message("송금이 완료되었습니다.")
                .amount(response.getAmount())
                .receiverName(receiver.getName())
                .receiverPhoneNumber(receiver.getPhoneNumber())
                .build();
    }
    
    @Transactional
    public ContactTransferResponse processUnregisteredUserTransfer(User sender, Account senderAccount,
                                                                   Contact contact, 
                                                                   ContactTransferRequest request) {
        // 1. 전화번호 검증
        PhoneVerificationResult verification = phoneVerificationService
                .verifyPhoneNumber(request.getReceiverPhoneNumber(), request.getReceiverName());
        
        if (!verification.isVerified()) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER, 
                    "전화번호 검증에 실패했습니다: " + verification.getMessage());
        }
        
        // 2. 이미 대기중인 송금이 있는지 확인
        List<PendingContactTransfer> activePending = pendingTransferRepository
                .findActivePendingTransfers(sender.getId(), request.getReceiverPhoneNumber());
        
        if (!activePending.isEmpty()) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSFER, 
                    "이미 해당 번호로 대기 중인 송금이 있습니다.");
        }
        
        // 3. 거래 ID 생성
        String transactionId = generateTransactionId();
        
        // 4. 잔액 홀드 (실제 차감은 하지 않음)
        balanceService.holdAmount(senderAccount.getAccountNumber(), request.getAmount());
        
        // 5. 대기 송금 생성
        PendingContactTransfer pendingTransfer = PendingContactTransfer.builder()
                .transactionId(transactionId)
                .sender(sender)
                .senderAccountNumber(senderAccount.getAccountNumber())
                .receiverPhoneNumber(request.getReceiverPhoneNumber())
                .receiverName(request.getReceiverName())
                .amount(request.getAmount())
                .memo(request.getMemo())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        // 6. 초대 코드 생성 및 SMS 발송
        String invitationCode = smsService.generateInvitationCode();
        pendingTransfer.markAsInvitationSent(invitationCode);
        
        pendingTransferRepository.save(pendingTransfer);
        
        // 7. SMS 발송
        boolean smsSent = smsService.sendInvitationSms(
                request.getReceiverPhoneNumber(),
                sender.getName(),
                request.getAmount(),
                invitationCode
        );
        
        if (!smsSent) {
            log.warn("SMS 발송 실패: {}", request.getReceiverPhoneNumber());
        }
        
        // 8. 감사 로그
        auditLogService.logSuccess(
                sender.getId(),
                sender.getPhoneNumber(),
                AuditEventType.TRANSFER_PENDING,
                String.format("비회원 송금 대기: %s (%s원)", 
                        request.getReceiverPhoneNumber(), request.getAmount()),
                null, null,
                String.format("transactionId: %s", transactionId),
                null
        );
        
        return ContactTransferResponse.builder()
                .transactionId(transactionId)
                .status("PENDING")
                .message("수신자에게 가입 안내 문자가 발송되었습니다. 24시간 내 가입하지 않으면 자동 취소됩니다.")
                .amount(request.getAmount())
                .receiverName(request.getReceiverName())
                .receiverPhoneNumber(request.getReceiverPhoneNumber())
                .expiresAt(pendingTransfer.getExpiresAt())
                .build();
    }
    
    @Transactional
    public void processPendingTransferOnRegistration(String phoneNumber) {
        // 신규 가입자의 대기 중인 송금 처리
        User newUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        List<PendingContactTransfer> pendingTransfers = pendingTransferRepository
                .findPendingByReceiverPhoneNumber(phoneNumber);
        
        for (PendingContactTransfer pending : pendingTransfers) {
            try {
                // 실제 송금 처리
                TransferRequest transferRequest = new TransferRequest();
                transferRequest.setReceiverAccountNumber(getReceiverAccount(newUser).getAccountNumber());
                transferRequest.setAmount(pending.getAmount());
                transferRequest.setMemo(pending.getMemo() + " (연락처 송금)");
                transferRequest.setSenderAccountNumber(pending.getSenderAccountNumber());
                
                TransferResponse response = transferService.transfer(
                        pending.getSender().getPhoneNumber(), 
                        transferRequest
                );
                
                // 대기 송금 상태 업데이트
                pending.markAsRegistered(newUser);
                pending.markAsCompleted(null); // Transfer 엔티티 연결은 나중에
                
                // 홀드 해제
                balanceService.releaseHold(pending.getSenderAccountNumber(), pending.getAmount());
                
                // 송금자에게 알림
                smsService.sendTransferNotification(
                        pending.getSender().getPhoneNumber(),
                        String.format("%s님이 가입하여 %s원이 송금되었습니다.", 
                                newUser.getName(), pending.getAmount())
                );
                
            } catch (Exception e) {
                log.error("대기 송금 처리 실패: {}", pending.getTransactionId(), e);
                pending.markAsCancelled("송금 처리 중 오류: " + e.getMessage());
                
                // 홀드 해제
                balanceService.releaseHold(pending.getSenderAccountNumber(), pending.getAmount());
            }
        }
    }
    
    @Scheduled(fixedDelay = 3600000) // 1시간마다 실행
    @Transactional
    public void processExpiredTransfers() {
        LocalDateTime now = LocalDateTime.now();
        List<PendingContactTransfer> expiredTransfers = pendingTransferRepository
                .findExpiredTransfers(now);
        
        for (PendingContactTransfer transfer : expiredTransfers) {
            try {
                // 만료 처리
                transfer.markAsExpired();
                
                // 홀드 해제
                balanceService.releaseHold(transfer.getSenderAccountNumber(), transfer.getAmount());
                
                // 송금자에게 알림
                smsService.sendCancellationNotification(
                        transfer.getSender().getPhoneNumber(),
                        transfer.getSender().getName(),
                        transfer.getAmount(),
                        "24시간 내 수령하지 않아 자동 취소됨"
                );
                
                // 감사 로그
                auditLogService.logSuccess(
                        transfer.getSender().getId(),
                        transfer.getSender().getPhoneNumber(),
                        AuditEventType.TRANSFER_CANCELLED,
                        String.format("송금 만료 취소: %s", transfer.getTransactionId()),
                        null, null,
                        null,
                        "24시간 만료"
                );
                
            } catch (Exception e) {
                log.error("만료 송금 처리 실패: {}", transfer.getTransactionId(), e);
            }
        }
    }
    
    @Transactional
    public void cancelPendingTransfer(String transactionId, String phoneNumber) {
        PendingContactTransfer pending = pendingTransferRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        
        // 권한 확인
        if (!pending.getSender().getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "송금 취소 권한이 없습니다.");
        }
        
        // 이미 완료/취소된 경우
        if (pending.getStatus() != PendingTransferStatus.PENDING && 
            pending.getStatus() != PendingTransferStatus.INVITATION_SENT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, 
                    "취소할 수 없는 상태입니다: " + pending.getStatus().getDescription());
        }
        
        // 취소 처리
        pending.markAsCancelled("송금자 요청");
        
        // 홀드 해제
        balanceService.releaseHold(pending.getSenderAccountNumber(), pending.getAmount());
        
        // 수신자에게 알림
        smsService.sendCancellationNotification(
                pending.getReceiverPhoneNumber(),
                pending.getSender().getName(),
                pending.getAmount(),
                "송금자가 취소함"
        );
    }
    
    private Contact createNewContact(User owner, ContactTransferRequest request) {
        Contact contact = Contact.builder()
                .owner(owner)
                .contactName(request.getReceiverName())
                .contactPhoneNumber(request.getReceiverPhoneNumber())
                .build();
        
        return contactRepository.save(contact);
    }
    
    private Account getSenderAccount(User sender, String accountNumber) {
        if (accountNumber != null && !accountNumber.trim().isEmpty()) {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
            
            if (!account.getUserId().equals(sender.getId())) {
                throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "본인 계좌가 아닙니다.");
            }
            
            return account;
        } else {
            return userAccountService.getPrimaryAccount(sender.getId())
                    .map(ua -> accountRepository.findByAccountNumber(ua.getAccountNumber())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)))
                    .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, 
                            "기본 계좌를 찾을 수 없습니다."));
        }
    }
    
    private Account getReceiverAccount(User receiver) {
        return userAccountService.getPrimaryAccount(receiver.getId())
                .map(ua -> accountRepository.findByAccountNumber(ua.getAccountNumber())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)))
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, 
                        "수신자의 기본 계좌를 찾을 수 없습니다."));
    }
    
    private String generateTransactionId() {
        String transactionId;
        do {
            transactionId = "CTX" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase();
        } while (pendingTransferRepository.existsByTransactionId(transactionId));
        
        return transactionId;
    }
    
    public PendingTransferResponse getPendingTransferStatus(String transactionId) {
        PendingContactTransfer pending = pendingTransferRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        
        return PendingTransferResponse.from(pending);
    }
}