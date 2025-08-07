package fintech2.easypay.auth.controller;

import fintech2.easypay.auth.dto.LoginRequest;
import fintech2.easypay.auth.dto.PasswordVerifyRequest;
import fintech2.easypay.auth.dto.RegisterRequest;
import fintech2.easypay.auth.dto.UserUpdateRequest;
import fintech2.easypay.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        return authService.checkEmailDuplicate(email);
    }
    
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhoneDuplicate(@RequestParam String phoneNumber) {
        return authService.checkPhoneDuplicate(phoneNumber);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return authService.getProfile();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest req) {
        return authService.updateProfile(req);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordVerifyRequest req) {
        return authService.verifyPassword(req);
    }

    @GetMapping("/check-pin-required")
    public ResponseEntity<?> checkPinRequired() {
        return authService.checkPinRequired();
    }
} 