package fintech2.easypay.contact.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public PhoneVerificationResult verifyPhoneNumber(String phoneNumber, String name) {
        try {
            // 실제 환경에서는 실제 전화번호 검증 API 호출
            // 예: NICE 본인인증 API, PASS 인증 API 등
            
            // Mock 구현
            return mockVerification(phoneNumber, name);
            
        } catch (Exception e) {
            log.error("전화번호 검증 실패: {}", e.getMessage());
            return PhoneVerificationResult.builder()
                    .verified(false)
                    .message("전화번호 검증 중 오류가 발생했습니다.")
                    .build();
        }
    }
    
    private PhoneVerificationResult mockVerification(String phoneNumber, String name) {
        // Mock 검증 로직
        // 실제로는 통신사 API를 통해 실명 확인
        
        // 테스트용 검증 로직
        if (phoneNumber.startsWith("010") && phoneNumber.length() == 11) {
            // 한국 휴대폰 번호 형식 확인
            
            // 테스트용: 특정 번호들은 검증 성공
            if (phoneNumber.equals("01012345678") || 
                phoneNumber.equals("01087654321") ||
                phoneNumber.equals("01099999999")) {
                
                return PhoneVerificationResult.builder()
                        .verified(true)
                        .phoneNumber(phoneNumber)
                        .name(name)
                        .carrier(getCarrier(phoneNumber))
                        .message("검증 성공")
                        .build();
            }
        }
        
        return PhoneVerificationResult.builder()
                .verified(false)
                .phoneNumber(phoneNumber)
                .message("전화번호와 이름이 일치하지 않습니다.")
                .build();
    }
    
    private String getCarrier(String phoneNumber) {
        // Mock 통신사 정보
        String prefix = phoneNumber.substring(0, 7);
        
        if (prefix.startsWith("010-1") || prefix.startsWith("010-2") || prefix.startsWith("010-3")) {
            return "SKT";
        } else if (prefix.startsWith("010-4") || prefix.startsWith("010-5") || prefix.startsWith("010-6")) {
            return "KT";
        } else {
            return "LGU+";
        }
    }
}