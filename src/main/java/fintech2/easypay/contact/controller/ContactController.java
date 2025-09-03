package fintech2.easypay.contact.controller;

import fintech2.easypay.common.response.ApiResponse;
import fintech2.easypay.contact.dto.ContactRequest;
import fintech2.easypay.contact.dto.ContactResponse;
import fintech2.easypay.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "연락처 관리 API")
public class ContactController {
    
    private final ContactService contactService;
    
    @PostMapping
    @Operation(summary = "연락처 추가", description = "새로운 연락처를 추가합니다.")
    public ResponseEntity<ApiResponse<ContactResponse>> addContact(
            Authentication authentication,
            @Valid @RequestBody ContactRequest request) {
        
        String phoneNumber = authentication.getName();
        ContactResponse response = contactService.addContact(phoneNumber, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "연락처가 추가되었습니다."));
    }
    
    @GetMapping
    @Operation(summary = "연락처 목록 조회", description = "사용자의 연락처 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<ContactResponse>>> getContacts(
            Authentication authentication,
            Pageable pageable) {
        
        String phoneNumber = authentication.getName();
        Page<ContactResponse> contacts = contactService.getContacts(phoneNumber, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "연락처 상세 조회", description = "특정 연락처의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ContactResponse>> getContact(
            Authentication authentication,
            @PathVariable Long id) {
        
        String phoneNumber = authentication.getName();
        ContactResponse contact = contactService.getContact(phoneNumber, id);
        
        return ResponseEntity.ok(ApiResponse.success(contact));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "연락처 수정", description = "연락처 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<ContactResponse>> updateContact(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        
        String phoneNumber = authentication.getName();
        ContactResponse response = contactService.updateContact(phoneNumber, id, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "연락처가 수정되었습니다."));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "연락처 삭제", description = "연락처를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            Authentication authentication,
            @PathVariable Long id) {
        
        String phoneNumber = authentication.getName();
        contactService.deleteContact(phoneNumber, id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "연락처가 삭제되었습니다."));
    }
}