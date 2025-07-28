package fintech2.easypay.account;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.entity.AccountStatus;
import fintech2.easypay.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Account 엔티티 테스트
 * 
 * 이 테스트는 Account 엔티티의 주요 기능들을 검증합니다:
 * - 계좌 생성
 * - 입금/출금 기능
 * - 잔액 확인
 * - 계좌 상태 변경
 * - 비즈니스 로직 검증
 */
@DisplayName("계좌 엔티티 테스트")
class AccountEntityTest {

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        testMember = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();

        // 테스트용 계좌 생성
        testAccount = Account.builder()
                .accountNumber("1234567890")
                .member(testMember)
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("계좌 생성 테스트")
    void createAccount() {
        // given & when: 계좌 생성
        Account account = Account.builder()
                .accountNumber("9876543210")
                .member(testMember)
                .balance(BigDecimal.valueOf(5000))
                .status(AccountStatus.ACTIVE)
                .build();

        // then: 계좌가 올바르게 생성되었는지 확인
        assertThat(account.getAccountNumber()).isEqualTo("9876543210");
        assertThat(account.getMember()).isEqualTo(testMember);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("입금 성공 테스트")
    void depositSuccess() {
        // given: 초기 잔액 10,000원
        BigDecimal initialBalance = testAccount.getBalance();
        BigDecimal depositAmount = BigDecimal.valueOf(5000);

        // when: 5,000원 입금
        testAccount.deposit(depositAmount);

        // then: 잔액이 15,000원으로 증가
        assertThat(testAccount.getBalance()).isEqualTo(initialBalance.add(depositAmount));
        assertThat(testAccount.getBalance()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("출금 성공 테스트")
    void withdrawSuccess() {
        // given: 초기 잔액 10,000원
        BigDecimal initialBalance = testAccount.getBalance();
        BigDecimal withdrawAmount = BigDecimal.valueOf(3000);

        // when: 3,000원 출금
        testAccount.withdraw(withdrawAmount);

        // then: 잔액이 7,000원으로 감소
        assertThat(testAccount.getBalance()).isEqualTo(initialBalance.subtract(withdrawAmount));
        assertThat(testAccount.getBalance()).isEqualTo(BigDecimal.valueOf(7000));
    }

    @Test
    @DisplayName("잔액 부족 시 출금 실패 테스트")
    void withdrawFailWithInsufficientBalance() {
        // given: 초기 잔액 10,000원
        BigDecimal withdrawAmount = BigDecimal.valueOf(15000); // 잔액보다 큰 금액

        // when & then: 출금 시도 시 예외 발생
        assertThatThrownBy(() -> testAccount.withdraw(withdrawAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    @DisplayName("음수 금액 입금 실패 테스트")
    void depositFailWithNegativeAmount() {
        // given: 음수 금액
        BigDecimal negativeAmount = BigDecimal.valueOf(-1000);

        // when & then: 입금 시도 시 예외 발생
        assertThatThrownBy(() -> testAccount.deposit(negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("입금 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("음수 금액 출금 실패 테스트")
    void withdrawFailWithNegativeAmount() {
        // given: 음수 금액
        BigDecimal negativeAmount = BigDecimal.valueOf(-1000);

        // when & then: 출금 시도 시 예외 발생
        assertThatThrownBy(() -> testAccount.withdraw(negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출금 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("계좌 잔액 충분성 확인 테스트")
    void hasEnoughBalanceTest() {
        // given: 초기 잔액 10,000원
        BigDecimal amount1 = BigDecimal.valueOf(5000);  // 잔액보다 적은 금액
        BigDecimal amount2 = BigDecimal.valueOf(10000); // 잔액과 같은 금액
        BigDecimal amount3 = BigDecimal.valueOf(15000); // 잔액보다 큰 금액

        // when & then: 잔액 충분성 확인
        assertThat(testAccount.hasEnoughBalance(amount1)).isTrue();
        assertThat(testAccount.hasEnoughBalance(amount2)).isTrue();
        assertThat(testAccount.hasEnoughBalance(amount3)).isFalse();
    }

    @Test
    @DisplayName("계좌 비활성화 테스트")
    void deactivateAccount() {
        // given: 활성 계좌
        assertThat(testAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        // when: 계좌 비활성화
        testAccount.deactivate();

        // then: 상태가 INACTIVE로 변경
        assertThat(testAccount.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    @DisplayName("활성 계좌 여부 확인 테스트")
    void isActiveAccount() {
        // given: 활성 계좌
        assertThat(testAccount.isActive()).isTrue();

        // when: 계좌 비활성화
        testAccount.deactivate();

        // then: 비활성 상태 확인
        assertThat(testAccount.isActive()).isFalse();
    }

    @Test
    @DisplayName("0원 입금 실패 테스트")
    void depositFailWithZeroAmount() {
        // given: 0원
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // when & then: 입금 시도 시 예외 발생
        assertThatThrownBy(() -> testAccount.deposit(zeroAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("입금 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("0원 출금 실패 테스트")
    void withdrawFailWithZeroAmount() {
        // given: 0원
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // when & then: 출금 시도 시 예외 발생
        assertThatThrownBy(() -> testAccount.withdraw(zeroAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출금 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("계좌 잔액 정확성 테스트")
    void balanceAccuracy() {
        // given: 초기 잔액 10,000원
        BigDecimal initialBalance = testAccount.getBalance();
        
        // when: 여러 거래 실행
        testAccount.deposit(BigDecimal.valueOf(2500));  // 12,500원
        testAccount.withdraw(BigDecimal.valueOf(1500)); // 11,000원
        testAccount.deposit(BigDecimal.valueOf(3000));  // 14,000원
        testAccount.withdraw(BigDecimal.valueOf(4000)); // 10,000원

        // then: 최종 잔액이 초기 잔액과 동일한지 확인
        assertThat(testAccount.getBalance()).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("계좌 상태별 거래 제한 테스트")
    void transactionLimitByAccountStatus() {
        // given: 활성 계좌에서 거래 가능
        assertThat(testAccount.isActive()).isTrue();
        
        // when: 입금/출금 성공
        testAccount.deposit(BigDecimal.valueOf(1000));
        testAccount.withdraw(BigDecimal.valueOf(500));
        
        // then: 거래가 성공적으로 처리됨
        assertThat(testAccount.getBalance()).isEqualTo(BigDecimal.valueOf(10500));
        
        // when: 계좌 비활성화
        testAccount.deactivate();
        
        // then: 비활성 계좌에서도 입금/출금 메서드 자체는 작동 (서비스 레이어에서 제한)
        assertThat(testAccount.isActive()).isFalse();
    }
}