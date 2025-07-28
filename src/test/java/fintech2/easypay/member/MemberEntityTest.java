package fintech2.easypay.member;

import fintech2.easypay.member.entity.Member;
import fintech2.easypay.member.entity.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Member 엔티티 테스트
 * 
 * 이 테스트는 Member 엔티티의 주요 기능들을 검증합니다:
 * - 빌더 패턴 생성
 * - 로그인 실패 카운트 관리
 * - 계정 잠금/해제 기능
 * - 비밀번호 변경
 * - 회원 상태 변경
 */
@DisplayName("회원 엔티티 테스트")
class MemberEntityTest {

    @Test
    @DisplayName("Member 객체 생성 테스트")
    void createMember() {
        // given & when: Member 객체 생성
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .email("test@example.com")
                .build();

        // then: 객체가 올바르게 생성되었는지 확인
        assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(member.getPassword()).isEqualTo("password123");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getLoginFailedCount()).isEqualTo(0);
        assertThat(member.isLocked()).isFalse();
    }

    @Test
    @DisplayName("로그인 실패 카운트 증가 테스트")
    void incrementLoginFailedCount() {
        // given: 새로운 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();

        // when: 로그인 실패 카운트 증가
        member.incrementLoginFailedCount();
        member.incrementLoginFailedCount();

        // then: 카운트가 증가했는지 확인
        assertThat(member.getLoginFailedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("로그인 실패 카운트 초기화 테스트")
    void resetLoginFailedCount() {
        // given: 로그인 실패 카운트가 있는 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();
        
        member.incrementLoginFailedCount();
        member.incrementLoginFailedCount();
        member.incrementLoginFailedCount();

        // when: 로그인 실패 카운트 초기화
        member.resetLoginFailedCount();

        // then: 카운트가 0으로 초기화되었는지 확인
        assertThat(member.getLoginFailedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("5회 실패 시 자동 계정 잠금 테스트")
    void autoLockAccountAfter5Failures() {
        // given: 새로운 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();

        // when: 5번 로그인 실패
        for (int i = 0; i < 5; i++) {
            member.incrementLoginFailedCount();
        }

        // then: 계정이 자동으로 잠겼는지 확인
        assertThat(member.isLocked()).isTrue();
        assertThat(member.getLoginFailedCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    void updatePassword() {
        // given: 기존 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("oldPassword")
                .name("홍길동")
                .build();

        // when: 비밀번호 변경
        member.updatePassword("newPassword");

        // then: 비밀번호가 변경되었는지 확인
        assertThat(member.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("회원 프로필 업데이트 테스트")
    void updateProfile() {
        // given: 기존 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .email("old@example.com")
                .build();

        // when: 프로필 업데이트
        member.updateProfile("김철수", "new@example.com");

        // then: 프로필이 업데이트되었는지 확인
        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void withdrawMember() {
        // given: 활성 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();

        // when: 회원 탈퇴
        member.withdraw();

        // then: 상태가 WITHDRAWN으로 변경되었는지 확인
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("활성 회원 여부 확인 테스트")
    void isActiveMember() {
        // given: 새로운 회원
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();

        // when & then: 활성 상태 확인
        assertThat(member.isActive()).isTrue();

        // when: 회원 탈퇴
        member.withdraw();

        // then: 비활성 상태 확인
        assertThat(member.isActive()).isFalse();
    }

    @Test
    @DisplayName("계정 잠금 해제 테스트")
    void unlockAccount() {
        // given: 잠긴 계정
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .password("password123")
                .name("홍길동")
                .build();
        
        // 5번 실패로 계정 잠금
        for (int i = 0; i < 5; i++) {
            member.incrementLoginFailedCount();
        }
        
        assertThat(member.isLocked()).isTrue();

        // when: 계정 잠금 해제 (성공적인 로그인 시 호출)
        member.resetLoginFailedCount();

        // then: 계정이 해제되고 실패 카운트가 초기화되었는지 확인
        assertThat(member.isLocked()).isFalse();
        assertThat(member.getLoginFailedCount()).isEqualTo(0);
    }
}