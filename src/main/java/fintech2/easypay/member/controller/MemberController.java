package fintech2.easypay.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fintech2.easypay.auth.CustomUserDetails;
import fintech2.easypay.common.ApiResponse;
import fintech2.easypay.member.dto.JwtResponse;
import fintech2.easypay.member.dto.MemberResponse;
import fintech2.easypay.member.dto.SignInRequest;
import fintech2.easypay.member.dto.SignUpRequest;
import fintech2.easypay.member.service.MemberService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "회원 관리", description = "회원 가입, 로그인, 프로필 관리 API")
public class MemberController {
    
    private final MemberService memberService;
    
    @PostMapping("/auth/signup")
    @Operation(summary = "회원 가입", description = "휴대폰 번호 기반 회원 가입")
    public ApiResponse<MemberResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ApiResponse.success("회원가입이 완료되었습니다.", response);
    }
    
    @PostMapping("/auth/signin")
    @Operation(summary = "로그인", description = "휴대폰 번호와 비밀번호로 로그인")
    public ApiResponse<JwtResponse> signIn(@Valid @RequestBody SignInRequest request) {
        JwtResponse response = memberService.signIn(request);
        return ApiResponse.success("로그인이 완료되었습니다.", response);
    }
    
    @GetMapping("/members/profile")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 조회")
    public ApiResponse<MemberResponse> getProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberResponse response = memberService.getProfile(userDetails.getUsername());
        return ApiResponse.success(response);
    }
    
    @PutMapping("/members/profile")
    @Operation(summary = "프로필 수정", description = "현재 로그인한 사용자의 프로필 수정")
    public ApiResponse<MemberResponse> updateProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String name,
        @RequestParam(required = false) String email) {
        MemberResponse response = 
            memberService.updateProfile(userDetails.getUsername(), name, email);
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }
    
    @DeleteMapping("/members/profile")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 회원 탈퇴")
    public ApiResponse<Void> withdraw(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.withdraw(userDetails.getUsername());
        return ApiResponse.success("회원 탈퇴가 완료되었습니다.", null);
    }
}
