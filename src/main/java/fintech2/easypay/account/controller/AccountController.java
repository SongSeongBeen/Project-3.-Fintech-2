package fintech2.easypay.account.controller;

import fintech2.easypay.account.service.AccountService;
import fintech2.easypay.auth.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber, @RequestHeader("Authorization") String token) {
        return accountService.getBalance(accountNumber, token);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getMyBalance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다"));
        }
        return accountService.getMyBalance(userPrincipal);
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debug(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("userPrincipal", userPrincipal);
        if (userPrincipal != null) {
            debugInfo.put("accountNumber", userPrincipal.getAccountNumber());
            debugInfo.put("phoneNumber", userPrincipal.getPhoneNumber());
            debugInfo.put("id", userPrincipal.getId());
        }
        return ResponseEntity.ok(debugInfo);
    }

    @GetMapping("/test-balance/{accountNumber}")
    public ResponseEntity<?> testBalance(@PathVariable String accountNumber) {
        try {
            return accountService.getBalance(accountNumber, "test-token");
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/update-balance")
    public ResponseEntity<?> updateBalance(@RequestBody Map<String, Object> request, 
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다"));
        }
        
        try {
            // 사용자 자신의 계좌번호 사용
            String accountNumber = userPrincipal.getAccountNumber();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String transactionType = (String) request.get("transactionType");
            String description = (String) request.get("description");
            
            String userId = userPrincipal.getId().toString();
            
            return accountService.updateBalance(accountNumber, amount, transactionType, description, userId);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "BALANCE_UPDATE_FAILED");
            error.put("message", "잔액 업데이트 실행");
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다"));
        }
        
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(400).body(Map.of("error", "INVALID_AMOUNT", "message", "입금 금액은 0보다 커야 합니다"));
            }
            
            return accountService.updateBalance(
                userPrincipal.getAccountNumber(), 
                amount, 
                "DEPOSIT", 
                "테스트 잔액 충전", 
                userPrincipal.getId().toString()
            );
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "DEPOSIT_FAILED");
            error.put("message", "입금 처리 중 오류가 발생했습니다");
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<?> getTransactionHistory(@PathVariable String accountNumber, @RequestHeader("Authorization") String token) {
        return accountService.getTransactionHistory(accountNumber);
    }
    
    @PostMapping("/sync-account-balance")
    public ResponseEntity<?> syncAccountBalance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다"));
        }
        
        try {
            return accountService.syncAccountBalance(userPrincipal.getAccountNumber());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SYNC_FAILED");
            error.put("message", "Account 엔티티 동기화 실패");
            return ResponseEntity.status(500).body(error);
        }
    }
} 