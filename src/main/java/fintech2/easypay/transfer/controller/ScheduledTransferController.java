package fintech2.easypay.transfer.controller;

import fintech2.easypay.common.response.ApiResponse;
import fintech2.easypay.transfer.dto.*;
import fintech2.easypay.transfer.service.ScheduledTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduled-transfers")
@RequiredArgsConstructor
@Tag(name = "Scheduled Transfer", description = "예약 송금 API")
public class ScheduledTransferController {
    
    private final ScheduledTransferService scheduledTransferService;
    
    @PostMapping
    @Operation(summary = "예약 송금 등록", description = "일회성 또는 반복 예약 송금을 등록합니다.")
    public ResponseEntity<ApiResponse<ScheduledTransferResponse>> createScheduledTransfer(
            Authentication authentication,
            @Valid @RequestBody ScheduledTransferRequest request) {
        
        String phoneNumber = authentication.getName();
        ScheduledTransferResponse response = scheduledTransferService
                .createScheduledTransfer(phoneNumber, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "예약 송금이 등록되었습니다."));
    }
    
    @GetMapping("/{scheduleId}")
    @Operation(summary = "예약 송금 조회", description = "특정 예약 송금의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ScheduledTransferResponse>> getScheduledTransfer(
            @PathVariable String scheduleId) {
        
        ScheduledTransferResponse response = scheduledTransferService.getScheduledTransfer(scheduleId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Operation(summary = "내 예약 송금 목록", description = "사용자의 예약 송금 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<ScheduledTransferResponse>>> getMyScheduledTransfers(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        
        String phoneNumber = authentication.getName();
        Page<ScheduledTransferResponse> response = scheduledTransferService
                .getMyScheduledTransfers(phoneNumber, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{scheduleId}")
    @Operation(summary = "예약 송금 수정", description = "예약 송금의 일부 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<ScheduledTransferResponse>> updateScheduledTransfer(
            Authentication authentication,
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduledTransferUpdateRequest request) {
        
        String phoneNumber = authentication.getName();
        ScheduledTransferResponse response = scheduledTransferService
                .updateScheduledTransfer(phoneNumber, scheduleId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "예약 송금이 수정되었습니다."));
    }
    
    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "예약 송금 취소", description = "예약 송금을 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelScheduledTransfer(
            Authentication authentication,
            @PathVariable String scheduleId) {
        
        String phoneNumber = authentication.getName();
        scheduledTransferService.cancelScheduledTransfer(phoneNumber, scheduleId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "예약 송금이 취소되었습니다."));
    }
    
    @PostMapping("/{scheduleId}/pause")
    @Operation(summary = "예약 송금 일시정지", description = "반복 예약 송금을 일시정지합니다.")
    public ResponseEntity<ApiResponse<Void>> pauseScheduledTransfer(
            Authentication authentication,
            @PathVariable String scheduleId) {
        
        String phoneNumber = authentication.getName();
        scheduledTransferService.pauseScheduledTransfer(phoneNumber, scheduleId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "예약 송금이 일시정지되었습니다."));
    }
    
    @PostMapping("/{scheduleId}/resume")
    @Operation(summary = "예약 송금 재개", description = "일시정지된 예약 송금을 재개합니다.")
    public ResponseEntity<ApiResponse<Void>> resumeScheduledTransfer(
            Authentication authentication,
            @PathVariable String scheduleId) {
        
        String phoneNumber = authentication.getName();
        scheduledTransferService.resumeScheduledTransfer(phoneNumber, scheduleId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "예약 송금이 재개되었습니다."));
    }
}