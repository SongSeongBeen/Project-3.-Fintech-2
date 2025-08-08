package fintech2.easypay.payment.service.impl;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.payment.service.PaymentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 결제 모듈용 회원 서비스 구현체
 * 실제 MemberRepository를 사용하여 회원 관련 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements PaymentMemberService {
    
    private final UserRepository userRepository;
    
    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByUsername(phoneNumber);
    }
    
    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}