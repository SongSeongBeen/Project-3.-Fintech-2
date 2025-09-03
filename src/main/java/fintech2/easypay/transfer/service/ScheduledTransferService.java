package fintech2.easypay.transfer.service;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.account.service.UserAccountService;
import fintech2.easypay.audit.service.AuditLogService;
import fintech2.easypay.audit.service.NotificationService;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.common.enums.AuditEventType;
import fintech2.easypay.transfer.dto.*;
import fintech2.easypay.transfer.entity.*;
import fintech2.easypay.transfer.repository.ScheduledTransferExecutionRepository;
import fintech2.easypay.transfer.repository.ScheduledTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduledTransferService {
    
    private final ScheduledTransferRepository scheduledTransferRepository;
    private final ScheduledTransferExecutionRepository executionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserAccountService userAccountService;
    private final BalanceService balanceService;
    private final TransferService transferService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    
    @Transactional
    public ScheduledTransferResponse createScheduledTransfer(String senderPhoneNumber, 
                                                            ScheduledTransferRequest request) {
        // 송금자 조회
        User sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 송금 계좌 조회
        Account senderAccount = getSenderAccount(sender, request.getSenderAccountNumber());
        
        // 수신 계좌 검증
        Account receiverAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ACCOUNT_NUMBER));
        
        // 예약 송금 개수 제한 (사용자당 최대 20개)
        long activeCount = scheduledTransferRepository.countActiveBySenderId(sender.getId());
        if (activeCount >= 20) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, "예약 송금은 최대 20개까지 등록 가능합니다.");
        }
        
        // 예약 ID 생성
        String scheduleId = generateScheduleId();
        
        // 다음 실행 시간 계산
        LocalDateTime nextExecutionTime = calculateNextExecutionTime(request);
        
        // 예약 송금 생성
        ScheduledTransfer scheduledTransfer = ScheduledTransfer.builder()
                .scheduleId(scheduleId)
                .sender(sender)
                .senderAccountNumber(senderAccount.getAccountNumber())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverName(request.getReceiverName())
                .amount(request.getAmount())
                .memo(request.getMemo())
                .scheduleType(request.getScheduleType())
                .repeatCycle(request.getRepeatCycle())
                .repeatDayOfMonth(request.getRepeatDayOfMonth())
                .repeatDayOfWeek(request.getRepeatDayOfWeek())
                .executionTime(request.getExecutionTime())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionTime(nextExecutionTime)
                .maxExecutionCount(request.getMaxExecutionCount())
                .notificationEnabled(request.isNotificationEnabled())
                .notificationMinutesBefore(request.getNotificationMinutesBefore())
                .build();
        
        scheduledTransferRepository.save(scheduledTransfer);
        
        // 감사 로그
        auditLogService.logSuccess(
                sender.getId(),
                senderPhoneNumber,
                AuditEventType.SCHEDULED_TRANSFER_CREATED,
                String.format("예약 송금 생성: %s -> %s (%s원)", 
                        senderAccount.getAccountNumber(), 
                        request.getReceiverAccountNumber(), 
                        request.getAmount()),
                null, null,
                String.format("scheduleId: %s, type: %s", scheduleId, request.getScheduleType()),
                null
        );
        
        return ScheduledTransferResponse.from(scheduledTransfer);
    }
    
    @Transactional
    public ScheduledTransferResponse updateScheduledTransfer(String phoneNumber, String scheduleId,
                                                            ScheduledTransferUpdateRequest request) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약 송금을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!scheduledTransfer.getSender().getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "수정 권한이 없습니다.");
        }
        
        // 완료/취소된 예약은 수정 불가
        if (scheduledTransfer.getStatus() == ScheduledTransferStatus.COMPLETED ||
            scheduledTransfer.getStatus() == ScheduledTransferStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "완료/취소된 예약은 수정할 수 없습니다.");
        }
        
        // 수정 가능한 필드 업데이트
        if (request.getAmount() != null) {
            scheduledTransfer.setAmount(request.getAmount());
        }
        if (request.getMemo() != null) {
            scheduledTransfer.setMemo(request.getMemo());
        }
        if (request.getExecutionTime() != null) {
            scheduledTransfer.setExecutionTime(request.getExecutionTime());
        }
        if (request.getEndDate() != null) {
            scheduledTransfer.setEndDate(request.getEndDate());
        }
        if (request.getNotificationEnabled() != null) {
            scheduledTransfer.setNotificationEnabled(request.getNotificationEnabled());
        }
        
        // 다음 실행 시간 재계산
        LocalDateTime nextExecutionTime = calculateNextExecutionTimeForUpdate(scheduledTransfer);
        scheduledTransfer.setNextExecutionTime(nextExecutionTime);
        
        scheduledTransferRepository.save(scheduledTransfer);
        
        return ScheduledTransferResponse.from(scheduledTransfer);
    }
    
    @Transactional
    public void cancelScheduledTransfer(String phoneNumber, String scheduleId) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약 송금을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!scheduledTransfer.getSender().getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "취소 권한이 없습니다.");
        }
        
        // 이미 취소/완료된 경우
        if (scheduledTransfer.getStatus() == ScheduledTransferStatus.CANCELLED ||
            scheduledTransfer.getStatus() == ScheduledTransferStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "이미 취소/완료된 예약입니다.");
        }
        
        scheduledTransfer.cancel();
        scheduledTransferRepository.save(scheduledTransfer);
        
        // 알림
        notificationService.sendTransferActivityNotification(
                scheduledTransfer.getSender().getId(),
                phoneNumber,
                String.format("예약 송금이 취소되었습니다. (ID: %s)", scheduleId)
        );
    }
    
    @Transactional
    public void pauseScheduledTransfer(String phoneNumber, String scheduleId) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약 송금을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!scheduledTransfer.getSender().getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "일시정지 권한이 없습니다.");
        }
        
        if (scheduledTransfer.getStatus() != ScheduledTransferStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "활성 상태의 예약만 일시정지할 수 있습니다.");
        }
        
        scheduledTransfer.pause();
        scheduledTransferRepository.save(scheduledTransfer);
    }
    
    @Transactional
    public void resumeScheduledTransfer(String phoneNumber, String scheduleId) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약 송금을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!scheduledTransfer.getSender().getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "재개 권한이 없습니다.");
        }
        
        if (scheduledTransfer.getStatus() != ScheduledTransferStatus.PAUSED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "일시정지 상태의 예약만 재개할 수 있습니다.");
        }
        
        scheduledTransfer.resume();
        
        // 다음 실행 시간 재계산
        LocalDateTime nextExecutionTime = calculateNextExecutionTimeForUpdate(scheduledTransfer);
        scheduledTransfer.setNextExecutionTime(nextExecutionTime);
        
        scheduledTransferRepository.save(scheduledTransfer);
    }
    
    /**
     * 예약 송금 실행 스케줄러 (1분마다 실행)
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void executeScheduledTransfers() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTransfer> transfersToExecute = scheduledTransferRepository
                .findScheduledTransfersToExecute(now);
        
        for (ScheduledTransfer scheduled : transfersToExecute) {
            try {
                executeScheduledTransfer(scheduled);
            } catch (Exception e) {
                log.error("예약 송금 실행 실패: {}", scheduled.getScheduleId(), e);
                handleExecutionFailure(scheduled, e.getMessage());
            }
        }
    }
    
    private void executeScheduledTransfer(ScheduledTransfer scheduled) {
        // 실행 기록 생성
        ScheduledTransferExecution execution = ScheduledTransferExecution.builder()
                .scheduledTransfer(scheduled)
                .executionTime(LocalDateTime.now())
                .amount(scheduled.getAmount())
                .status(ExecutionStatus.PROCESSING)
                .build();
        
        executionRepository.save(execution);
        
        try {
            // 송금 실행
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setReceiverAccountNumber(scheduled.getReceiverAccountNumber());
            transferRequest.setAmount(scheduled.getAmount());
            transferRequest.setMemo(scheduled.getMemo() + " [예약송금]");
            transferRequest.setSenderAccountNumber(scheduled.getSenderAccountNumber());
            
            TransferResponse response = transferService.transfer(
                    scheduled.getSender().getPhoneNumber(), 
                    transferRequest
            );
            
            // 실행 성공 처리
            execution.markAsSuccess(null); // Transfer 엔티티는 나중에 연결
            executionRepository.save(execution);
            
            scheduled.incrementExecutionCount();
            scheduled.resetFailureCount();
            
            // 완료 여부 확인
            scheduled.complete();
            
            // 다음 실행 시간 계산 (반복 송금인 경우)
            if (scheduled.getScheduleType() == ScheduleType.RECURRING && scheduled.isActive()) {
                LocalDateTime nextExecutionTime = calculateNextExecutionTimeForUpdate(scheduled);
                scheduled.setNextExecutionTime(nextExecutionTime);
            }
            
            scheduledTransferRepository.save(scheduled);
            
            // 알림
            notificationService.sendTransferActivityNotification(
                    scheduled.getSender().getId(),
                    scheduled.getSender().getPhoneNumber(),
                    String.format("예약 송금이 실행되었습니다. %s원 -> %s", 
                            scheduled.getAmount(), scheduled.getReceiverAccountNumber())
            );
            
        } catch (BusinessException e) {
            // 실행 실패 처리
            execution.markAsFailed(e.getMessage());
            executionRepository.save(execution);
            
            handleExecutionFailure(scheduled, e.getMessage());
        }
    }
    
    private void handleExecutionFailure(ScheduledTransfer scheduled, String errorMessage) {
        scheduled.incrementFailureCount(errorMessage);
        
        // 잔액 부족인 경우 재시도 스케줄링
        if (errorMessage.contains("잔액") && scheduled.getFailureCount() < 3) {
            // 30분 후 재시도
            scheduled.setNextExecutionTime(LocalDateTime.now().plusMinutes(30));
        } else if (scheduled.getStatus() == ScheduledTransferStatus.FAILED) {
            // 3회 실패 시 비활성화
            notificationService.sendTransferActivityNotification(
                    scheduled.getSender().getId(),
                    scheduled.getSender().getPhoneNumber(),
                    String.format("예약 송금이 3회 실패하여 비활성화되었습니다. (ID: %s)", 
                            scheduled.getScheduleId())
            );
        }
        
        scheduledTransferRepository.save(scheduled);
    }
    
    /**
     * 예약 송금 알림 스케줄러 (5분마다 실행)
     */
    @Scheduled(fixedDelay = 300000)
    public void sendScheduledTransferNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(25);
        LocalDateTime endTime = now.plusMinutes(35);
        
        List<ScheduledTransfer> transfersToNotify = scheduledTransferRepository
                .findTransfersForNotification(startTime, endTime);
        
        for (ScheduledTransfer scheduled : transfersToNotify) {
            try {
                notificationService.sendTransferActivityNotification(
                        scheduled.getSender().getId(),
                        scheduled.getSender().getPhoneNumber(),
                        String.format("30분 후 예약 송금이 실행됩니다. %s원 -> %s", 
                                scheduled.getAmount(), scheduled.getReceiverAccountNumber())
                );
            } catch (Exception e) {
                log.error("예약 송금 알림 실패: {}", scheduled.getScheduleId(), e);
            }
        }
    }
    
    private LocalDateTime calculateNextExecutionTime(ScheduledTransferRequest request) {
        LocalDateTime baseTime = request.getStartDate();
        
        if (request.getScheduleType() == ScheduleType.ONE_TIME) {
            return baseTime;
        }
        
        // 반복 송금인 경우
        return calculateRecurringExecutionTime(
                baseTime,
                request.getExecutionTime(),
                request.getRepeatCycle(),
                request.getRepeatDayOfMonth(),
                request.getRepeatDayOfWeek()
        );
    }
    
    private LocalDateTime calculateNextExecutionTimeForUpdate(ScheduledTransfer scheduled) {
        LocalDateTime baseTime = LocalDateTime.now();
        
        if (scheduled.getScheduleType() == ScheduleType.ONE_TIME) {
            return scheduled.getNextExecutionTime();
        }
        
        return calculateRecurringExecutionTime(
                baseTime,
                scheduled.getExecutionTime(),
                scheduled.getRepeatCycle(),
                scheduled.getRepeatDayOfMonth(),
                scheduled.getRepeatDayOfWeek()
        );
    }
    
    private LocalDateTime calculateRecurringExecutionTime(LocalDateTime baseTime, 
                                                         LocalTime executionTime,
                                                         RepeatCycle cycle,
                                                         Integer dayOfMonth,
                                                         Integer dayOfWeek) {
        LocalDateTime nextExecution = baseTime.with(executionTime);
        
        switch (cycle) {
            case DAILY:
                if (nextExecution.isBefore(LocalDateTime.now())) {
                    nextExecution = nextExecution.plusDays(1);
                }
                break;
                
            case WEEKLY:
                if (dayOfWeek != null) {
                    nextExecution = nextExecution.with(
                            TemporalAdjusters.next(DayOfWeek.of(dayOfWeek))
                    );
                }
                break;
                
            case MONTHLY:
                if (dayOfMonth != null) {
                    nextExecution = nextExecution.withDayOfMonth(
                            Math.min(dayOfMonth, nextExecution.toLocalDate().lengthOfMonth())
                    );
                    if (nextExecution.isBefore(LocalDateTime.now())) {
                        nextExecution = nextExecution.plusMonths(1);
                    }
                }
                break;
                
            case YEARLY:
                if (nextExecution.isBefore(LocalDateTime.now())) {
                    nextExecution = nextExecution.plusYears(1);
                }
                break;
        }
        
        return nextExecution;
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
    
    private String generateScheduleId() {
        String scheduleId;
        do {
            scheduleId = "SCH" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase();
        } while (scheduledTransferRepository.existsByScheduleId(scheduleId));
        
        return scheduleId;
    }
    
    public ScheduledTransferResponse getScheduledTransfer(String scheduleId) {
        ScheduledTransfer scheduled = scheduledTransferRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약 송금을 찾을 수 없습니다."));
        
        return ScheduledTransferResponse.from(scheduled);
    }
    
    public Page<ScheduledTransferResponse> getMyScheduledTransfers(String phoneNumber, Pageable pageable) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Page<ScheduledTransfer> scheduledTransfers = scheduledTransferRepository
                .findBySenderId(user.getId(), pageable);
        
        return scheduledTransfers.map(ScheduledTransferResponse::from);
    }
}