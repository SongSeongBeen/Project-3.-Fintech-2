package fintech2.easypay.auth.dto;

import fintech2.easypay.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String phoneNumber;
    private String password;
    private String accountNumber;
    
    public static UserPrincipal from(User user) {
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
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return phoneNumber;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
} 