package fintech2.easypay.transfer.service;

import fintech2.easypay.transfer.dto.ScheduledTransferRequest;
import fintech2.easypay.transfer.entity.RepeatCycle;
import fintech2.easypay.transfer.entity.ScheduleType;
import fintech2.easypay.transfer.entity.ScheduledTransfer;
import fintech2.easypay.transfer.repository.ScheduledTransferRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledTransferServiceTest {
    
    @Mock
    private ScheduledTransferRepository scheduledTransferRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private UserAccountService userAccountService;
    
    @InjectMocks
    private ScheduledTransferService scheduledTransferService;
    
    @Test
    @DisplayName("일회성 예약 송금 등록 - 요청 검증")
    void createOneTimeScheduledTransfer_RequestValidation() {
        // Given
        ScheduledTransferRequest request = ScheduledTransferRequest.builder()
                .receiverAccountNumber("1111111111")
                .receiverName("홍길동")
                .amount(BigDecimal.valueOf(50000))
                .memo("용돈")
                .scheduleType(ScheduleType.ONE_TIME)
                .executionTime(LocalTime.of(10, 0))
                .startDate(LocalDateTime.now().plusDays(1))
                .build();
        
        // Then
        assertThat(request.getScheduleType()).isEqualTo(ScheduleType.ONE_TIME);
        assertThat(request.getAmount()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(request.getExecutionTime()).isEqualTo(LocalTime.of(10, 0));
    }
    
    @Test
    @DisplayName("반복 예약 송금 등록 - 요청 검증")
    void createRecurringScheduledTransfer_RequestValidation() {
        // Given
        ScheduledTransferRequest request = ScheduledTransferRequest.builder()
                .receiverAccountNumber("2222222222")
                .receiverName("김철수")
                .amount(BigDecimal.valueOf(100000))
                .memo("정기 송금")
                .scheduleType(ScheduleType.RECURRING)
                .repeatCycle(RepeatCycle.MONTHLY)
                .repeatDayOfMonth(1)
                .executionTime(LocalTime.of(9, 0))
                .startDate(LocalDateTime.now().plusMonths(1))
                .maxExecutionCount(12)
                .build();
        
        // Then
        assertThat(request.getScheduleType()).isEqualTo(ScheduleType.RECURRING);
        assertThat(request.getRepeatCycle()).isEqualTo(RepeatCycle.MONTHLY);
        assertThat(request.getRepeatDayOfMonth()).isEqualTo(1);
        assertThat(request.getMaxExecutionCount()).isEqualTo(12);
    }
}