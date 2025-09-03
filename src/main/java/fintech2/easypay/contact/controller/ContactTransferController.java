package fintech2.easypay.contact.controller;

import fintech2.easypay.common.response.ApiResponse;
import fintech2.easypay.contact.dto.ContactTransferRequest;
import fintech2.easypay.contact.dto.ContactTransferResponse;
import fintech2.easypay.contact.dto.PendingTransferResponse;
import fintech2.easypay.contact.service.ContactTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-transfer")
@RequiredArgsConstructor
@Tag(name = "Contact Transfer", description = "연락처 송금 API")
public class ContactTransferController {
    
    private final ContactTransferService contactTransferService;
    
    @PostMapping
    @Operation(summary = "연락처로 송금", description = "전화번호를 통한 송금 (회원/비회원)")
    public ResponseEntity<ApiResponse<ContactTransferResponse>> transferToContact(
            Authentication authentication,
            @Valid @RequestBody ContactTransferRequest request) {
        
        String phoneNumber = authentication.getName();
        ContactTransferResponse response = contactTransferService.transferToContact(phoneNumber, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/pending/{transactionId}")
    @Operation(summary = "대기 송금 상태 조회", description = "대기 중인 송금의 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<PendingTransferResponse>> getPendingTransferStatus(
            @PathVariable String transactionId) {
        
        PendingTransferResponse response = contactTransferService.getPendingTransferStatus(transactionId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/pending/{transactionId}/cancel")
    @Operation(summary = "대기 송금 취소", description = "대기 중인 송금을 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelPendingTransfer(
            Authentication authentication,
            @PathVariable String transactionId) {
        
        String phoneNumber = authentication.getName();
        contactTransferService.cancelPendingTransfer(transactionId, phoneNumber);
        
        return ResponseEntity.ok(ApiResponse.success(null, "송금이 취소되었습니다."));
    }
}