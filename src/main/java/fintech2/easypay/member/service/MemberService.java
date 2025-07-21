package fintech2.easypay.member.service;

import fintech2.easypay.auth.JwtTokenProvider;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.member.dto.JwtResponse;
import fintech2.easypay.member.dto.MemberResponse;
import fintech2.easypay.member.dto.SignInRequest;
import fintech2.easypay.member.dto.SignUpRequest;
import fintech2.easypay.member.entity.Member;
import fintech2.easypay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관리 서비스
 * 회원가입, 로그인, 프로필 관리 기능을 제공
 * JWT 기반 인증과 비밀번호 암호화 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 회원가입 처리
     * 휴대폰 번호 중복 확인과 비밀번호 암호화
     * @param request 회원가입 요청 정보
     * @return 가입된 회원 정보
     * @throws BusinessException 휴대폰 번호가 이미 존재하는 경우
     */
    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        // 휴대폰 번호 중복 확인
        if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
        
        // 비밀번호 암호화 (BCrypt 사용)
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 회원 생성
        Member member = Member.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .name(request.getName())
                .email(request.getEmail())
                .build();
        
        Member savedMember = memberRepository.save(member);
        
        log.info("회원가입 완료: {}", savedMember.getPhoneNumber());
        
        return MemberResponse.from(savedMember);
    }
    
    /**
     * 로그인 처리
     * 비밀번호 검증과 계정 잠금 상태 확인
     * 로그인 실패 시 다음 시도를 위한 잠금 수준 관리
     * @param request 로그인 요청 정보
     * @return JWT 토큰과 회원 정보
     * @throws BusinessException 로그인 실패 또는 계정 잠금 시
     */
    @Transactional
    public JwtResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 계정 잠금 상태 확인
        if (member.isLocked()) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "계정이 잠겨있습니다. 관리자에게 문의하세요.");
        }
        
        // 비밀번호 검증 (BCrypt 사용)
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            member.incrementLoginFailedCount();
            memberRepository.save(member);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // 로그인 성공 시 실패 횟수 초기화
        member.resetLoginFailedCount();
        memberRepository.save(member);
        
        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(member.getPhoneNumber());
        
        log.info("로그인 성공: {}", member.getPhoneNumber());
        
        return JwtResponse.of(token, jwtTokenProvider.getTokenValidityInMilliseconds(), 
                             MemberResponse.from(member));
    }
    
    public MemberResponse getProfile(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        return MemberResponse.from(member);
    }
    
    @Transactional
    public MemberResponse updateProfile(String phoneNumber, String name, String email) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.updateProfile(name, email);
        
        return MemberResponse.from(member);
    }
    
    @Transactional
    public void withdraw(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.withdraw();
        
        log.info("회원 탈퇴: {}", phoneNumber);
    }
}
