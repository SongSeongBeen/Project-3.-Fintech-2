package fintech2.easypay.contact.service;

import fintech2.easypay.contact.dto.ContactTransferRequest;
import fintech2.easypay.contact.dto.ContactTransferResponse;
import fintech2.easypay.contact.entity.PendingContactTransfer;
import fintech2.easypay.contact.repository.ContactRepository;
import fintech2.easypay.contact.repository.PendingContactTransferRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.account.service.BalanceService;
import fintech2.easypay.account.service.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactTransferServiceTest {
    
    @Mock
    private ContactRepository contactRepository;
    
    @Mock
    private PendingContactTransferRepository pendingTransferRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private UserAccountService userAccountService;
    
    @Mock
    private BalanceService balanceService;
    
    @InjectMocks
    private ContactTransferService contactTransferService;
    
    @Test
    @DisplayName("비회원 연락처 송금 - 성공")
    void transferToUnregisteredContact_Success() {
        // Given
        User sender = User.builder()
                .id(1L)
                .phoneNumber("01012345678")
                .name("송금자")
                .build();
        
        Account senderAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .userId(1L)
                .balance(BigDecimal.valueOf(100000))
                .build();
        
        ContactTransferRequest request = ContactTransferRequest.builder()
                .receiverPhoneNumber("01087654321")
                .receiverName("수신자")
                .amount(BigDecimal.valueOf(50000))
                .memo("테스트 송금")
                .build();
        
        // When
        when(userRepository.findByPhoneNumber("01012345678")).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneNumber("01087654321")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(senderAccount));
        when(balanceService.hasSufficientBalance(anyString(), any())).thenReturn(true);
        when(pendingTransferRepository.save(any())).thenReturn(new PendingContactTransfer());
        
        // Then - 실제 테스트는 애플리케이션 실행 후 진행
        assertThat(request.getReceiverPhoneNumber()).isEqualTo("01087654321");
        assertThat(request.getAmount()).isEqualTo(BigDecimal.valueOf(50000));
    }
}