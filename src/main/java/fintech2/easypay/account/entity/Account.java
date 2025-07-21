package fintech2.easypay.account.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import fintech2.easypay.common.BaseEntity;
import fintech2.easypay.member.entity.Member;

/**
 * 계좌 정보를 관리하는 엔티티 클래스
 * 사용자의 가상계좌 및 잔액 정보를 관리
 */
@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {
    
    /**
     * 계좌 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 계좌번호 (고유)
     */
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    
    /**
     * 계좌 소유자 (회원과 1:1 매핑)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    /**
     * 계좌 잔액 (정밀도 19자리, 소수점 2자리)
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    /**
     * 계좌 상태 (활성, 비활성 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;
    
    /**
     * 낙관적 락킹을 위한 버전 필드
     * 동시성 제어를 통한 잔액 변경 시 데이터 일관성 보장
     */
    @Version
    private Long version;
    
    /**
     * 계좌에 입금 처리
     * @param amount 입금할 금액
     * @throws IllegalArgumentException 입금 금액이 0 이하인 경우
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }
    
    /**
     * 계좌에서 출금 처리
     * @param amount 출금할 금액
     * @throws IllegalArgumentException 출금 금액이 0 이하이거나 잔액이 부족한 경우
     */
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("출금 금액은 0보다 커야 합니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    /**
     * 충분한 잔액이 있는지 확인
     * @param amount 확인할 금액
     * @return 잔액 충분 여부
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * 계좌 비활성화
     */
    public void deactivate() {
        this.status = AccountStatus.INACTIVE;
    }
    
    /**
     * 계좌 활성 상태 확인
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
