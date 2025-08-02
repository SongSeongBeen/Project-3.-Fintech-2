package fintech2.easypay.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User Builder로 객체 생성 테스트")
    void createUserWithBuilder() {
        // Given
        Long id = 1L;
        String phoneNumber = "010-1234-5678";
        String email = "test@example.com";
        String password = "encryptedPassword";
        String name = "홍길동";
        LocalDateTime createdAt = LocalDateTime.now();
        String accountNumber = "VA1234567890";

        // When
        User user = User.builder()
                .id(id)
                .phoneNumber(phoneNumber)
                .email(email)
                .password(password)
                .name(name)
                .createdAt(createdAt)
                .accountNumber(accountNumber)
                .build();

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getLockExpiresAt()).isNull();
        assertThat(user.getLockReason()).isNull();
    }

    @Test
    @DisplayName("User Builder 패턴으로 객체 생성 테스트")
    void createUserWithBuilder2() {
        // Given
        String name = "테스트사용자";
        String phoneNumber = "010-9876-5432";
        String email = "test2@example.com";
        String accountNumber = "VA9876543210";
        String password = "hashedPassword123";

        // When
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .email(email)
                .password(password)
                .name(name)
                .accountNumber(accountNumber)
                .build();

        // Then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getLoginFailCount()).isEqualTo(0);
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("로그인 실패 횟수 증가 테스트")
    void incrementLoginFailCount() {
        // Given
        User user = User.builder()
                .phoneNumber("010-1111-2222")
                .email("test3@example.com")
                .password("password")
                .name("사용자")
                .accountNumber("VA1111222233")
                .build();

        // When
        user.incrementLoginFailCount();

        // Then
        assertThat(user.getLoginFailCount()).isEqualTo(1);
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("로그인 5회 실패 시 계정 잠금 테스트")
    void lockAccountAfterFiveFailures() {
        // Given
        User user = User.builder()
                .phoneNumber("010-1111-2222")
                .email("test4@example.com")
                .password("password")
                .name("사용자")
                .accountNumber("VA1111222233")
                .build();

        // When
        for (int i = 0; i < 5; i++) {
            user.incrementLoginFailCount();
        }

        // Then
        assertThat(user.getLoginFailCount()).isEqualTo(5);
        assertThat(user.isLocked()).isTrue();
        assertThat(user.getLockExpiresAt()).isNotNull();
        assertThat(user.getLockReason()).isEqualTo("로그인 5회 실패로 인한 계정 잠금");
        assertThat(user.isAccountLocked()).isTrue();
    }

    @Test
    @DisplayName("로그인 실패 횟수 리셋 테스트")
    void resetLoginFailCount() {
        // Given
        User user = User.builder()
                .phoneNumber("010-1111-2222")
                .email("test5@example.com")
                .password("password")
                .name("사용자")
                .accountNumber("VA1111222233")
                .build();
        
        user.incrementLoginFailCount();
        user.incrementLoginFailCount();

        // When
        user.resetLoginFailCount();

        // Then
        assertThat(user.getLoginFailCount()).isEqualTo(0);
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getLockExpiresAt()).isNull();
        assertThat(user.getLockReason()).isNull();
    }

    @Test
    @DisplayName("VirtualAccount 객체 반환 테스트")
    void getVirtualAccount() {
        // Given
        User user = User.builder()
                .phoneNumber("010-1111-2222")
                .email("test6@example.com")
                .password("password")
                .name("사용자")
                .accountNumber("VA1111222233")
                .build();

        // When
        var virtualAccount = user.getVirtualAccount();

        // Then
        assertThat(virtualAccount).isNotNull();
        assertThat(virtualAccount.getAccountNumber()).isEqualTo("VA1111222233");
    }

    @Test
    @DisplayName("PIN 관련 기능 테스트")
    void pinRelatedTests() {
        // Given
        User user = User.builder()
                .phoneNumber("010-1111-2222")
                .email("test7@example.com")
                .password("password")
                .name("사용자")
                .transferPin("$2a$10$encryptedPin")
                .build();

        // When & Then
        assertThat(user.hasPinSet()).isTrue();
        assertThat(user.getPinFailCount()).isEqualTo(0);
        assertThat(user.isPinLocked()).isFalse();

        // PIN 실패 횟수 증가 테스트
        user.incrementPinFailCount();
        assertThat(user.getPinFailCount()).isEqualTo(1);

        // PIN 리셋 테스트
        user.resetPinFailCount();
        assertThat(user.getPinFailCount()).isEqualTo(0);
        assertThat(user.isPinLocked()).isFalse();
    }
}