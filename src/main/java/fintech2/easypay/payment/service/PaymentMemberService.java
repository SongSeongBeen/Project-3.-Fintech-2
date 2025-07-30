package fintech2.easypay.payment.service;

import fintech2.easypay.auth.entity.User;

import java.util.Optional;

/**
 * 결제 모듈에서 회원 관련 기능을 위한 추상화 인터페이스
 * 실제 MemberRepository 의존성을 제거하고 테스트 가능하도록 분리
 */
public interface PaymentMemberService { // MemberService -> PaymentMemberService
    
    /**
     * 휴대폰 번호로 회원 조회
     */
    Optional<User> findByPhoneNumber(String phoneNumber); // Member -> User
    
    /**
     * 회원 ID로 회원 조회
     */
    Optional<User> findById(Long userId); // Member -> User, memberId -> userId
}