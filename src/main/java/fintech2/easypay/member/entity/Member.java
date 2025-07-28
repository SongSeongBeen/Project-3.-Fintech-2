package fintech2.easypay.member.entity;

import fintech2.easypay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 정보를 관리하는 엔티티 클래스
 * 사용자의 기본 정보와 계정 상태를 관리
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    
    /**
     * 회원 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 휴대폰 번호 (로그인 아이디 역할)
     */
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    
    /**
     * 암호화된 비밀번호
     */
    @Column(name = "password", nullable = false)
    private String password;
    
    /**
     * 회원 이름
     */
    @Column(name = "name", nullable = false)
    private String name;
    
    /**
     * 이메일 주소 (선택사항)
     */
    @Column(name = "email")
    private String email;
    
    /**
     * 회원 상태 (활성, 비활성, 탈퇴 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    
    /**
     * 로그인 실패 횟수 (보안을 위한 계정 잠금 관리)
     */
    @Column(name = "login_failed_count")
    @Builder.Default
    private int loginFailedCount = 0;
    
    /**
     * 계정 잠금 상태
     */
    @Column(name = "is_locked")
    @Builder.Default
    private boolean isLocked = false;
    
    /**
     * 로그인 실패 횟수를 증가시키고 5회 이상 실패 시 계정을 잠금 처리
     * 보안을 위한 브루트 포스 공격 방지 메커니즘
     */
    public void incrementLoginFailedCount() {
        this.loginFailedCount++;
        if (this.loginFailedCount >= 5) {
            this.isLocked = true;
        }
    }
    
    /**
     * 로그인 실패 횟수를 초기화하고 계정 잠금을 해제
     * 성공적인 로그인 시 호출
     */
    public void resetLoginFailedCount() {
        this.loginFailedCount = 0;
        this.isLocked = false;
    }
    
    /**
     * 비밀번호 변경
     * @param newPassword 새로운 암호화된 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
    
    /**
     * 프로필 정보 업데이트
     * @param name 변경할 이름
     * @param email 변경할 이메일 주소
     */
    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    /**
     * 회원 탈퇴 처리
     * 상태를 WITHDRAWN으로 변경
     */
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }
    
    /**
     * 회원 활성 상태 확인
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }
}
