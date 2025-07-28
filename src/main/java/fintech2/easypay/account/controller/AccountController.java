package fintech2.easypay.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fintech2.easypay.account.dto.AccountResponse;
import fintech2.easypay.account.dto.DepositRequest;
import fintech2.easypay.account.dto.WithdrawRequest;
import fintech2.easypay.account.service.AccountService;
import fintech2.easypay.auth.CustomUserDetails;
import fintech2.easypay.common.ApiResponse;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "계좌 관리", description = "가상계좌 및 잔액 관리 API")
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping
    @Operation(summary = "계좌 생성", description = "사용자의 가상계좌 생성")
    public ApiResponse<AccountResponse> createAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        AccountResponse response = accountService.createAccount(userDetails.getUsername());
        return ApiResponse.success("계좌가 생성되었습니다.", response);
    }
    
    @GetMapping
    @Operation(summary = "계좌 조회", description = "현재 사용자의 계좌 정보 조회")
    public ApiResponse<AccountResponse> getAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        AccountResponse response = accountService.getAccount(userDetails.getUsername());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{accountNumber}")
    @Operation(summary = "계좌번호로 조회", description = "계좌번호로 계좌 정보 조회")
    public ApiResponse<AccountResponse> getAccountByNumber(
        @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/deposit")
    @Operation(summary = "입금", description = "계좌에 입금")
    public ApiResponse<AccountResponse> deposit(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody DepositRequest request) {
        AccountResponse response = accountService.deposit(userDetails.getUsername(), request);
        return ApiResponse.success("입금이 완료되었습니다.", response);
    }
    
    @PostMapping("/withdraw")
    @Operation(summary = "출금", description = "계좌에서 출금")
    public ApiResponse<AccountResponse> withdraw(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody WithdrawRequest request) {
        AccountResponse response = accountService.withdraw(userDetails.getUsername(), request);
        return ApiResponse.success("출금이 완료되었습니다.", response);
    }
    
    @PostMapping("/reset")
    @Operation(summary = "잔액 초기화", description = "테스트용 잔액 초기화 (100만원)")
    public ApiResponse<Void> resetBalance(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        accountService.resetBalance(userDetails.getUsername());
        return ApiResponse.success("잔액이 초기화되었습니다.", null);
    }
}
