package fintech2.easypay.integration;

import fintech2.easypay.account.entity.Account;
import fintech2.easypay.account.repository.AccountRepository;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.auth.service.AuthService;
import fintech2.easypay.common.enums.AccountStatus;
import fintech2.easypay.payment.dto.PaymentRequest;
import fintech2.easypay.payment.entity.PaymentMethod;
import fintech2.easypay.payment.service.PaymentService;
import fintech2.easypay.transfer.dto.TransferRequest;
import fintech2.easypay.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("한국 연예인 통합 시나리오 테스트")
class CelebrityIntegrationTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuthService authService;
    @Mock private PaymentService paymentService;
    @Mock private TransferService transferService;

    // 한국 연예인 가상 사용자들
    private User parkBoGum;      // 박보검 - 톱스타
    private User shinSeKyung;    // 신세경 - 베테랑 배우
    private User chaEunWoo;      // 차은우 - 아이돌 배우
    private User karina;         // 카리나 - K-POP 스타
    private User kimSeonHo;      // 김선호 - 드라마 스타
    private User suzy;           // 수지 - 국민첫사랑

    // 연예인 계좌들
    private Account parkBoGumAccount;
    private Account shinSeKyungAccount;  
    private Account chaEunWooAccount;
    private Account karinaAccount;
    private Account kimSeonHoAccount;
    private Account suzyAccount;

    @BeforeEach
    void setUp() {
        // Given: 한국 연예인 가상 사용자 데이터 생성
        parkBoGum = new User(
            1L,
            "010-1234-1234",
            "encodedPasswordBogum",
            "박보검",
            LocalDateTime.now().minusYears(5),
            "VA1234123412",
            0,
            false,
            null,
            null
        );

        shinSeKyung = new User(
            2L,
            "010-5678-5678", 
            "encodedPasswordSeKyung",
            "신세경",
            LocalDateTime.now().minusYears(7),
            "VA5678567856",
            0,
            false,
            null,
            null
        );

        chaEunWoo = new User(
            3L,
            "010-9999-1111",
            "encodedPasswordEunWoo",
            "차은우",
            LocalDateTime.now().minusYears(3),
            "VA9999111199",
            0,
            false,
            null,
            null
        );

        karina = new User(
            4L,
            "010-1111-9999",
            "encodedPasswordKarina", 
            "카리나",
            LocalDateTime.now().minusYears(2),
            "VA1111999911",
            0,
            false,
            null,
            null
        );

        kimSeonHo = new User(
            5L,
            "010-7777-8888",
            "encodedPasswordSeonHo",
            "김선호",
            LocalDateTime.now().minusYears(4),
            "VA7777888877",
            0,
            false,
            null,
            null
        );

        suzy = new User(
            6L,
            "010-2222-3333",
            "encodedPasswordSuzy",
            "수지",
            LocalDateTime.now().minusYears(6),
            "VA2222333322",
            0,
            false,
            null,
            null
        );

        // Given: 연예인 계좌 데이터 생성 (연예인답게 높은 잔액)
        parkBoGumAccount = Account.builder()
            .id(1L)
            .accountNumber("VA1234123412")
            .userId(1L)
            .balance(new BigDecimal("50000000"))  // 5천만원 (톱스타)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(5))
            .build();

        shinSeKyungAccount = Account.builder()
            .id(2L)
            .accountNumber("VA5678567856")
            .userId(2L)
            .balance(new BigDecimal("30000000"))  // 3천만원 (베테랑)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(7))
            .build();

        chaEunWooAccount = Account.builder()
            .id(3L)
            .accountNumber("VA9999111199")
            .userId(3L)
            .balance(new BigDecimal("25000000"))  // 2천5백만원 (아이돌 배우)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(3))
            .build();

        karinaAccount = Account.builder()
            .id(4L)
            .accountNumber("VA1111999911")
            .userId(4L)
            .balance(new BigDecimal("20000000"))  // 2천만원 (K-POP 스타)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(2))
            .build();

        kimSeonHoAccount = Account.builder()
            .id(5L)
            .accountNumber("VA7777888877")
            .userId(5L)
            .balance(new BigDecimal("40000000"))  // 4천만원 (드라마 스타)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(4))
            .build();

        suzyAccount = Account.builder()
            .id(6L)
            .accountNumber("VA2222333322")
            .userId(6L)
            .balance(new BigDecimal("60000000"))  // 6천만원 (국민첫사랑)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusYears(6))
            .build();
    }

    @Test
    @DisplayName("시나리오 1: 박보검이 신세경에게 생일 축하금 1천만원 송금")
    void parkBoGumToShinSeKyungBirthdayGift() {
        // Given: 박보검이 신세경 선배에게 생일 축하금을 보내는 시나리오
        TransferRequest request = new TransferRequest();
        request.setReceiverAccountNumber("VA5678567856");
        request.setAmount(new BigDecimal("10000000"));
        request.setMemo("신세경 선배님 생일 축하드려요! 🎉");

        // When: 송금 실행 (잔액 변화 시뮬레이션)
        parkBoGumAccount.withdraw(request.getAmount());
        shinSeKyungAccount.deposit(request.getAmount());

        // Then: 송금 결과 검증
        assertThat(parkBoGumAccount.getBalance()).isEqualTo(new BigDecimal("40000000")); // 5천만 - 1천만
        assertThat(shinSeKyungAccount.getBalance()).isEqualTo(new BigDecimal("40000000")); // 3천만 + 1천만
        assertThat(request.getMemo()).contains("생일 축하");
        assertThat(parkBoGum.getName()).isEqualTo("박보검");
        assertThat(shinSeKyung.getName()).isEqualTo("신세경");
    }

    @Test
    @DisplayName("시나리오 2: 차은우가 명품 브랜드에서 5백만원 결제")
    void chaEunWooLuxuryShoppingPayment() {
        // Given: 차은우가 명품 쇼핑하는 시나리오
        PaymentRequest request = new PaymentRequest();
        request.setMerchantId("LUXURY_BRAND_001");
        request.setMerchantName("구찌 청담점");
        request.setAmount(new BigDecimal("5000000"));
        request.setMemo("화보 촬영용 의상");
        request.setPaymentMethod(PaymentMethod.BALANCE);

        // When: 결제 실행 (잔액 변화 시뮬레이션)
        chaEunWooAccount.withdraw(request.getAmount());

        // Then: 결제 결과 검증
        assertThat(chaEunWooAccount.getBalance()).isEqualTo(new BigDecimal("20000000")); // 2천5백만 - 5백만
        assertThat(request.getMerchantName()).isEqualTo("구찌 청담점");
        assertThat(request.getMemo()).contains("화보 촬영용");
        assertThat(chaEunWoo.getName()).isEqualTo("차은우");
    }

    @Test
    @DisplayName("시나리오 3: 카리나가 후배들에게 커피 쏘는 소액 다중 송금")
    void karinaTreatJuniorsMultipleTransfers() {
        // Given: 카리나가 후배들에게 커피값을 보내는 시나리오
        BigDecimal coffeeAmount = new BigDecimal("50000"); // 5만원씩
        int juniorCount = 10; // 후배 10명
        BigDecimal totalAmount = coffeeAmount.multiply(new BigDecimal(juniorCount));

        // When: 다중 송금 시뮬레이션
        for (int i = 0; i < juniorCount; i++) {
            karinaAccount.withdraw(coffeeAmount);
        }

        // Then: 다중 송금 결과 검증
        assertThat(karinaAccount.getBalance()).isEqualTo(new BigDecimal("19500000")); // 2천만 - 50만
        assertThat(totalAmount).isEqualTo(new BigDecimal("500000")); // 총 50만원
        assertThat(karina.getName()).isEqualTo("카리나");
    }

    @Test
    @DisplayName("시나리오 4: 김선호가 자선단체에 거액 기부")
    void kimSeonHoCharityDonation() {
        // Given: 김선호가 자선단체에 기부하는 시나리오
        PaymentRequest donationRequest = new PaymentRequest();
        donationRequest.setMerchantId("CHARITY_ORG_001");
        donationRequest.setMerchantName("사랑의열매 사회복지공동모금회");
        donationRequest.setAmount(new BigDecimal("15000000"));
        donationRequest.setMemo("어려운 이웃들을 위한 기부");
        donationRequest.setPaymentMethod(PaymentMethod.BALANCE);

        // When: 기부 결제 실행
        kimSeonHoAccount.withdraw(donationRequest.getAmount());

        // Then: 기부 결제 결과 검증
        assertThat(kimSeonHoAccount.getBalance()).isEqualTo(new BigDecimal("25000000")); // 4천만 - 1천5백만
        assertThat(donationRequest.getMerchantName()).contains("사회복지공동모금회");
        assertThat(donationRequest.getMemo()).contains("기부");
        assertThat(kimSeonHo.getName()).isEqualTo("김선호");
    }

    @Test
    @DisplayName("시나리오 5: 수지가 팬미팅 경품으로 팬들에게 선물 송금")
    void suzyFanMeetingPrizeTransfers() {
        // Given: 수지가 팬미팅에서 팬들에게 선물금을 보내는 시나리오
        BigDecimal prizeAmount = new BigDecimal("1000000"); // 100만원씩
        int winnerCount = 5; // 당첨자 5명
        BigDecimal totalPrize = prizeAmount.multiply(new BigDecimal(winnerCount));

        // When: 팬미팅 선물 송금 실행
        for (int i = 0; i < winnerCount; i++) {
            suzyAccount.withdraw(prizeAmount);
        }

        // Then: 선물 송금 결과 검증
        assertThat(suzyAccount.getBalance()).isEqualTo(new BigDecimal("55000000")); // 6천만 - 5백만
        assertThat(totalPrize).isEqualTo(new BigDecimal("5000000")); // 총 5백만원
        assertThat(suzy.getName()).isEqualTo("수지");
    }

    @Test
    @DisplayName("시나리오 6: 연예인들의 합동 자선 프로젝트 - 복합 송금")
    void celebrityJointCharityProject() {
        // Given: 연예인들이 합동으로 자선 프로젝트에 참여하는 시나리오
        BigDecimal parkBoGumDonation = new BigDecimal("10000000");  // 박보검 1천만
        BigDecimal shinSeKyungDonation = new BigDecimal("8000000");  // 신세경 8백만
        BigDecimal chaEunWooDonation = new BigDecimal("7000000");   // 차은우 7백만
        BigDecimal karinaDonation = new BigDecimal("5000000");      // 카리나 5백만
        BigDecimal kimSeonHoDonation = new BigDecimal("12000000");  // 김선호 1천2백만
        BigDecimal suzyDonation = new BigDecimal("15000000");       // 수지 1천5백만

        // When: 각 연예인별 자선 기부 실행
        parkBoGumAccount.withdraw(parkBoGumDonation);
        shinSeKyungAccount.withdraw(shinSeKyungDonation);
        chaEunWooAccount.withdraw(chaEunWooDonation);
        karinaAccount.withdraw(karinaDonation);
        kimSeonHoAccount.withdraw(kimSeonHoDonation);
        suzyAccount.withdraw(suzyDonation);

        // 총 기부금 계산
        BigDecimal totalDonation = parkBoGumDonation
            .add(shinSeKyungDonation)
            .add(chaEunWooDonation)
            .add(karinaDonation)
            .add(kimSeonHoDonation)
            .add(suzyDonation);

        // Then: 합동 자선 프로젝트 결과 검증
        assertThat(totalDonation).isEqualTo(new BigDecimal("57000000")); // 총 5천7백만원
        assertThat(parkBoGumAccount.getBalance()).isEqualTo(new BigDecimal("40000000"));
        assertThat(shinSeKyungAccount.getBalance()).isEqualTo(new BigDecimal("22000000"));
        assertThat(chaEunWooAccount.getBalance()).isEqualTo(new BigDecimal("18000000"));
        assertThat(karinaAccount.getBalance()).isEqualTo(new BigDecimal("15000000"));
        assertThat(kimSeonHoAccount.getBalance()).isEqualTo(new BigDecimal("28000000"));
        assertThat(suzyAccount.getBalance()).isEqualTo(new BigDecimal("45000000"));

        // 모든 연예인이 기부에 참여했는지 확인
        assertThat(parkBoGum.getName()).isEqualTo("박보검");
        assertThat(shinSeKyung.getName()).isEqualTo("신세경");
        assertThat(chaEunWoo.getName()).isEqualTo("차은우");
        assertThat(karina.getName()).isEqualTo("카리나");
        assertThat(kimSeonHo.getName()).isEqualTo("김선호");
        assertThat(suzy.getName()).isEqualTo("수지");
    }

    @Test
    @DisplayName("시나리오 7: 연예인 계좌 상태 및 보안 검증")
    void celebrityAccountSecurityVerification() {
        // Given: 연예인 계좌들의 보안 상태를 검증하는 시나리오

        // When & Then: 각 연예인 계좌 보안 상태 검증
        
        // 1. 모든 계좌가 활성 상태인지 확인
        assertThat(parkBoGumAccount.isActive()).isTrue();
        assertThat(shinSeKyungAccount.isActive()).isTrue();
        assertThat(chaEunWooAccount.isActive()).isTrue();
        assertThat(karinaAccount.isActive()).isTrue();
        assertThat(kimSeonHoAccount.isActive()).isTrue();
        assertThat(suzyAccount.isActive()).isTrue();

        // 2. 모든 사용자가 계정 잠금 상태가 아닌지 확인
        assertThat(parkBoGum.isAccountLocked()).isFalse();
        assertThat(shinSeKyung.isAccountLocked()).isFalse();
        assertThat(chaEunWoo.isAccountLocked()).isFalse();
        assertThat(karina.isAccountLocked()).isFalse();
        assertThat(kimSeonHo.isAccountLocked()).isFalse();
        assertThat(suzy.isAccountLocked()).isFalse();

        // 3. 로그인 실패 횟수가 0인지 확인
        assertThat(parkBoGum.getLoginFailCount()).isEqualTo(0);
        assertThat(shinSeKyung.getLoginFailCount()).isEqualTo(0);
        assertThat(chaEunWoo.getLoginFailCount()).isEqualTo(0);
        assertThat(karina.getLoginFailCount()).isEqualTo(0);
        assertThat(kimSeonHo.getLoginFailCount()).isEqualTo(0);
        assertThat(suzy.getLoginFailCount()).isEqualTo(0);

        // 4. 계좌번호 형식이 올바른지 확인 (VA로 시작하는 12자리)
        assertThat(parkBoGumAccount.getAccountNumber()).matches("VA\\d{10}");
        assertThat(shinSeKyungAccount.getAccountNumber()).matches("VA\\d{10}");
        assertThat(chaEunWooAccount.getAccountNumber()).matches("VA\\d{10}");
        assertThat(karinaAccount.getAccountNumber()).matches("VA\\d{10}");
        assertThat(kimSeonHoAccount.getAccountNumber()).matches("VA\\d{10}");
        assertThat(suzyAccount.getAccountNumber()).matches("VA\\d{10}");
    }
}