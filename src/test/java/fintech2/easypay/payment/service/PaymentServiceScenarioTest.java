package fintech2.easypay.payment.service;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.payment.entity.Payment;
import fintech2.easypay.payment.entity.PaymentMethod;
import fintech2.easypay.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 서비스 실제 시나리오 테스트")
class PaymentServiceScenarioTest {

    // 가상 사용자들
    private User customer1; // 일반 고객
    private User customer2; // VIP 고객
    private User customer3; // 신규 고객

    // 가상 결제 데이터
    private Payment coffeePayment;
    private Payment bookPayment;
    private Payment taxiPayment;
    private Payment largePayment;

    @BeforeEach
    void setUp() {
        // Given: 가상 고객 데이터 생성
        customer1 = new User(
            1L,
            "010-1111-1111",
            "encodedPassword1",
            "카페고객",
            LocalDateTime.now().minusDays(100),
            "VA1111111111",
            0,
            false,
            null,
            null
        );

        customer2 = new User(
            2L,
            "010-2222-2222",
            "encodedPassword2",
            "VIP고객",
            LocalDateTime.now().minusDays(365),
            "VA2222222222",
            0,
            false,
            null,
            null
        );

        customer3 = new User(
            3L,
            "010-3333-3333",
            "encodedPassword3",
            "신규고객",
            LocalDateTime.now().minusDays(1),
            "VA3333333333",
            0,
            false,
            null,
            null
        );

        // Given: 다양한 결제 시나리오 데이터 생성
        coffeePayment = Payment.builder()
            .id(1L)
            .paymentId("PAY_COFFEE_001")
            .user(customer1)
            .accountNumber(customer1.getAccountNumber())
            .merchantId("MERCHANT_CAFE")
            .merchantName("스타벅스 강남점")
            .amount(new BigDecimal("4500"))
            .memo("아메리카노 2잔")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();

        bookPayment = Payment.builder()
            .id(2L)
            .paymentId("PAY_BOOK_002")
            .user(customer2)
            .accountNumber(customer2.getAccountNumber())
            .merchantId("MERCHANT_BOOK")
            .merchantName("교보문고")
            .amount(new BigDecimal("25000"))
            .memo("프로그래밍 서적 구매")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();

        taxiPayment = Payment.builder()
            .id(3L)
            .paymentId("PAY_TAXI_003")
            .user(customer3)
            .accountNumber(customer3.getAccountNumber())
            .merchantId("MERCHANT_TAXI")
            .merchantName("카카오택시")
            .amount(new BigDecimal("12800"))
            .memo("강남역 → 홍대입구")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();

        largePayment = Payment.builder()
            .id(4L)
            .paymentId("PAY_LARGE_004")
            .user(customer2)
            .accountNumber(customer2.getAccountNumber())
            .merchantId("MERCHANT_ELECTRONICS")
            .merchantName("삼성전자 매장")
            .amount(new BigDecimal("1500000"))
            .memo("노트북 구매")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();
    }

    @Test
    @DisplayName("시나리오 1: 일반 고객 카페 결제 성공")
    void coffeePaymentSuccessScenario() {
        // Given: 카페고객이 커피를 주문하는 시나리오
        // 이미 setUp에서 데이터 준비됨

        // When: 결제가 승인되는 과정
        coffeePayment.markAsProcessing();
        coffeePayment.markAsApproved("PG_TXN_001", "결제 승인 완료");

        // Then: 결제 결과 검증
        assertThat(coffeePayment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(coffeePayment.getUser().getName()).isEqualTo("카페고객");
        assertThat(coffeePayment.getMerchantName()).isEqualTo("스타벅스 강남점");
        assertThat(coffeePayment.getAmount()).isEqualTo(new BigDecimal("4500"));
        assertThat(coffeePayment.getPgTransactionId()).isEqualTo("PG_TXN_001");
        assertThat(coffeePayment.isApproved()).isTrue();
        assertThat(coffeePayment.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("시나리오 2: VIP 고객 도서 구매 결제")
    void bookPaymentVipCustomerScenario() {
        // Given: VIP고객이 책을 구매하는 시나리오
        // 이미 setUp에서 데이터 준비됨

        // When: VIP 고객의 결제 처리
        bookPayment.markAsProcessing();
        bookPayment.markAsApproved("PG_TXN_002", "VIP 고객 우대 결제 승인");

        // Then: VIP 고객 결제 결과 검증
        assertThat(bookPayment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(bookPayment.getUser().getName()).isEqualTo("VIP고객");
        assertThat(bookPayment.getMerchantName()).isEqualTo("교보문고");
        assertThat(bookPayment.getAmount()).isEqualTo(new BigDecimal("25000"));
        assertThat(bookPayment.getMemo()).contains("프로그래밍 서적");
        assertThat(bookPayment.canBeCancelled()).isTrue();
    }

    @Test
    @DisplayName("시나리오 3: 신규 고객 택시 결제 실패")
    void taxiPaymentNewCustomerFailureScenario() {
        // Given: 신규고객이 택시비를 결제하려는 시나리오
        // 잔액 부족으로 실패하는 상황

        // When: 결제 실패 처리
        taxiPayment.markAsProcessing();
        taxiPayment.markAsFailed("잔액 부족으로 결제 실패");

        // Then: 결제 실패 결과 검증
        assertThat(taxiPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(taxiPayment.getUser().getName()).isEqualTo("신규고객");
        assertThat(taxiPayment.getMerchantName()).isEqualTo("카카오택시");
        assertThat(taxiPayment.getFailedReason()).isEqualTo("잔액 부족으로 결제 실패");
        assertThat(taxiPayment.isFailed()).isTrue();
        assertThat(taxiPayment.getApprovedAt()).isNull();
    }

    @Test
    @DisplayName("시나리오 4: 고액 결제 후 취소")
    void largePaymentCancellationScenario() {
        // Given: VIP고객이 고액 상품을 구매한 후 취소하는 시나리오
        
        // When: 결제 승인 후 취소 처리
        largePayment.markAsProcessing();
        largePayment.markAsApproved("PG_TXN_004", "고액 결제 승인");
        
        // 고객이 마음을 바꿔서 취소 요청
        largePayment.markAsCancelled();

        // Then: 결제 취소 결과 검증
        assertThat(largePayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(largePayment.getAmount()).isEqualTo(new BigDecimal("1500000"));
        assertThat(largePayment.isCancelled()).isTrue();
        assertThat(largePayment.getCancelledAt()).isNotNull();
        assertThat(largePayment.getApprovedAt()).isNotNull(); // 원래 승인된 시점은 유지
    }

    @Test
    @DisplayName("시나리오 5: 결제 승인 후 부분 환불")
    void paymentRefundScenario() {
        // Given: 결제 승인된 상품에 대한 환불 시나리오
        Payment refundPayment = Payment.builder()
            .id(5L)
            .paymentId("PAY_REFUND_005")
            .user(customer1)
            .accountNumber(customer1.getAccountNumber())
            .merchantId("MERCHANT_ONLINE")
            .merchantName("쿠팡")
            .amount(new BigDecimal("89000"))
            .memo("운동화 구매")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();

        // When: 결제 승인 후 환불 처리
        refundPayment.markAsProcessing();
        refundPayment.markAsApproved("PG_TXN_005", "온라인 쇼핑 결제 승인");
        
        // 고객이 상품 불량으로 환불 요청
        refundPayment.markAsRefunded();

        // Then: 환불 처리 결과 검증
        assertThat(refundPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refundPayment.isRefunded()).isTrue();
        assertThat(refundPayment.getRefundedAt()).isNotNull();
        assertThat(refundPayment.canBeRefunded()).isFalse(); // 이미 환불된 상태
    }

    @Test
    @DisplayName("시나리오 6: 복합 고객 결제 패턴 분석")
    void multipleCustomerPaymentPatternScenario() {
        // Given: 여러 고객의 다양한 결제 패턴 시나리오
        
        // When: 각 고객별 결제 패턴 실행
        // 카페고객 - 소액 결제 성공
        coffeePayment.markAsProcessing();
        coffeePayment.markAsApproved("PG_001", "소액 결제 승인");

        // VIP고객 - 도서 결제 성공
        bookPayment.markAsProcessing();
        bookPayment.markAsApproved("PG_002", "도서 결제 승인");

        // 신규고객 - 택시 결제 실패
        taxiPayment.markAsProcessing();
        taxiPayment.markAsFailed("신규 고객 한도 초과");

        // Then: 고객별 결제 패턴 분석
        // 일반 고객 (카페고객)
        assertThat(coffeePayment.isApproved()).isTrue();
        assertThat(coffeePayment.getAmount().compareTo(new BigDecimal("10000"))).isLessThan(0); // 소액결제

        // VIP 고객
        assertThat(bookPayment.isApproved()).isTrue();
        assertThat(bookPayment.getUser().getCreatedAt()).isBefore(LocalDateTime.now().minusDays(300)); // 장기고객

        // 신규 고객
        assertThat(taxiPayment.isFailed()).isTrue();
        assertThat(taxiPayment.getUser().getCreatedAt()).isAfter(LocalDateTime.now().minusDays(7)); // 신규고객
    }

    @Test
    @DisplayName("시나리오 7: 결제 상태 변화 추적")
    void paymentStatusTransitionScenario() {
        // Given: 결제 상태 변화를 추적하는 시나리오
        Payment trackingPayment = Payment.builder()
            .id(7L)
            .paymentId("PAY_TRACK_007")
            .user(customer2)
            .accountNumber(customer2.getAccountNumber())
            .merchantId("MERCHANT_FASHION")
            .merchantName("H&M")
            .amount(new BigDecimal("45000"))
            .memo("겨울 코트")
            .paymentMethod(PaymentMethod.BALANCE)
            .status(PaymentStatus.REQUESTED)
            .build();

        // When: 결제 상태 단계별 변화
        // 1단계: 요청됨 → 처리중
        assertThat(trackingPayment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
        trackingPayment.markAsProcessing();
        
        // 2단계: 처리중 → 승인됨
        assertThat(trackingPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        trackingPayment.markAsApproved("PG_TRACK_007", "패션 아이템 결제 승인");

        // Then: 최종 상태 검증
        assertThat(trackingPayment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(trackingPayment.isApproved()).isTrue();
        assertThat(trackingPayment.canBeCancelled()).isTrue();
        assertThat(trackingPayment.canBeRefunded()).isTrue();
    }
}