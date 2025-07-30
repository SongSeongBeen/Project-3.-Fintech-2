package fintech2.easypay.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User 생성자로 객체 생성 테스트")
    void createUserWithConstructor() {
        // Given
        Long id = 1L;
        String phoneNumber = "010-1234-5678";
        String password = "encryptedPassword";
        String name = "홍길동";
        LocalDateTime createdAt = LocalDateTime.now();
        String accountNumber = "VA1234567890";
        Integer loginFailCount = 0;
        boolean isLocked = false;
        LocalDateTime lockExpiresAt = null;
        String lockReason = null;

        // When
        User user = new User(id, phoneNumber, password, name, createdAt, accountNumber, 
                           loginFailCount, isLocked, lockExpiresAt, lockReason);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getLockExpiresAt()).isNull();
        assertThat(user.getLockReason()).isNull();
    }

    @Test
    @DisplayName("User 생성자 패턴으로 객체 생성 테스트")
    void createUserWithConstructor2() {
        // Given
        String name = "테스트사용자";
        String phoneNumber = "010-9876-5432";
        String accountNumber = "VA9876543210";
        String password = "hashedPassword123";

        // When
        User user = new User(null, phoneNumber, password, name, LocalDateTime.now(), 
                           accountNumber, 0, false, null, null);

        // Then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getLoginFailCount()).isEqualTo(0);
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("로그인 실패 횟수 증가 테스트")
    void incrementLoginFailCount() {
        // Given
        User user = new User(null, "010-1111-2222", "password", "사용자", 
                           LocalDateTime.now(), "VA1111222233", 0, false, null, null);

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
        User user = new User(null, "010-1111-2222", "password", "사용자", 
                           LocalDateTime.now(), "VA1111222233", 0, false, null, null);

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
        User user = new User(null, "010-1111-2222", "password", "사용자", 
                           LocalDateTime.now(), "VA1111222233", 0, false, null, null);
        
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
        User user = new User(null, "010-1111-2222", "password", "사용자", 
                           LocalDateTime.now(), "VA1111222233", 0, false, null, null);

        // When
        var virtualAccount = user.getVirtualAccount();

        // Then
        assertThat(virtualAccount).isNotNull();
        assertThat(virtualAccount.getAccountNumber()).isEqualTo("VA1111222233");
    }
}