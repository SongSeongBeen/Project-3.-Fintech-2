package fintech2.easypay.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String type;
    private Long expiresIn;
    private MemberResponse member;
    
    public static JwtResponse of(String token, Long expiresIn, MemberResponse member) {
        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(expiresIn)
                .member(member)
                .build();
    }
}
