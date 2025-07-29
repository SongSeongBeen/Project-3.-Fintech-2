package fintech2.easypay.auth.service;

import fintech2.easypay.auth.dto.UserPrincipal;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + phoneNumber));
        
        // Hibernate 세션 문제를 피하기 위해 직접 UserPrincipal 생성
        // 외부 변경 가능 객체 문제 해결을 위해 문자열 복사
        String safePhoneNumber = new String(user.getPhoneNumber());
        String safePassword = new String(user.getPassword());
        String safeAccountNumber = user.getVirtualAccount() != null ? 
            new String(user.getVirtualAccount().getAccountNumber()) : null;
        
        return UserPrincipal.builder()
                .id(user.getId())
                .phoneNumber(safePhoneNumber)
                .password(safePassword)
                .accountNumber(safeAccountNumber)
                .build();
    }
} 