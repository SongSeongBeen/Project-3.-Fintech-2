package fintech2.easypay.account.entity;

import fintech2.easypay.common.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 통합 계좌 엔티티
 * VirtualAccount와 AccountBalance 기능을 통합
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false) // memberId -> userId 통일
    private Long userId; // Member -> User로 변경에 따라 userId 사용

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Version
    private Integer version; // 낙관적 락을 위한 버전 필드

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 비즈니스 메소드들 (기존 payment/transfer 코드에서 사용)
    
    /**
     * 잔액 확인
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    /**
     * 출금 (결제, 송금 시 사용)
     */
    public void withdraw(BigDecimal amount) {
        if (!hasEnoughBalance(amount)) {
            throw new IllegalArgumentException("잔액 부족: 현재 잔액 " + this.balance + ", 요청 금액 " + amount);
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * 입금 (환불, 송금 수신 시 사용)
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다: " + amount);
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * 잔액 조회
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * 계좌 활성화 여부 확인
     */
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    /**
     * 계좌 비활성화
     */
    public void deactivate() {
        this.status = AccountStatus.INACTIVE;
    }

    /**
     * 계좌 활성화
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }
}