package fintech2.easypay.auth.entity;

import fintech2.easypay.account.entity.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private String accountNumber; // 가상계좌번호 (1:1)

    // 계정 잠금 관련 필드
    @Builder.Default
    private Integer loginFailCount = 0;
    @Builder.Default
    private boolean isLocked = false;
    private LocalDateTime lockExpiresAt;
    private String lockReason;

    // PIN 인증 관련 필드
    @Column(nullable = true)
    private String transferPin; // BCrypt 암호화된 6자리 PIN
    
    @Builder.Default
    private Integer pinFailCount = 0;
    @Builder.Default
    private boolean isPinLocked = false;
    private LocalDateTime pinLockExpiresAt;
    private String pinLockReason;
    private LocalDateTime pinCreatedAt;
    private LocalDateTime pinLastUsedAt;

    // VirtualAccount 객체 반환 (호환성을 위해)
    public VirtualAccount getVirtualAccount() {
        if (accountNumber != null) {
            return VirtualAccount.builder()
                .accountNumber(accountNumber)
                .build();
        }
        return null;
    }

    public void incrementLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.isLocked = true;
            this.lockExpiresAt = LocalDateTime.now().plusMinutes(30);
            this.lockReason = "로그인 5회 실패로 인한 계정 잠금";
        }
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.isLocked = false;
        this.lockExpiresAt = null;
        this.lockReason = null;
    }

    public boolean isAccountLocked() {
        if (!isLocked) return false;
        if (lockExpiresAt != null && LocalDateTime.now().isAfter(lockExpiresAt)) {
            resetLoginFailCount();
            return false;
        }
        return true;
    }

    // PIN 인증 관련 메서드
    public void incrementPinFailCount() {
        this.pinFailCount++;
        if (this.pinFailCount >= 5) {
            this.isPinLocked = true;
            this.pinLockExpiresAt = LocalDateTime.now().plusMinutes(30);
            this.pinLockReason = "PIN 5회 실패로 인한 PIN 잠금";
        }
    }

    public void resetPinFailCount() {
        this.pinFailCount = 0;
        this.isPinLocked = false;
        this.pinLockExpiresAt = null;
        this.pinLockReason = null;
    }

    public boolean isPinLocked() {
        if (!isPinLocked) return false;
        if (pinLockExpiresAt != null && LocalDateTime.now().isAfter(pinLockExpiresAt)) {
            resetPinFailCount();
            return false;
        }
        return true;
    }

    public void updatePinLastUsed() {
        this.pinLastUsedAt = LocalDateTime.now();
    }

    public boolean hasPinSet() {
        return transferPin != null && !transferPin.isEmpty();
    }
} 