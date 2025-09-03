package fintech2.easypay.contact.dto;

import fintech2.easypay.contact.entity.Contact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "연락처 응답")
public class ContactResponse {
    
    @Schema(description = "연락처 ID", example = "1")
    private Long id;
    
    @Schema(description = "연락처 이름", example = "김친구")
    private String contactName;
    
    @Schema(description = "연락처 전화번호", example = "01087654321")
    private String contactPhoneNumber;
    
    @Schema(description = "등록된 사용자 여부", example = "true")
    private boolean isRegistered;
    
    @Schema(description = "인증된 연락처 여부", example = "false")
    private boolean isVerified;
    
    @Schema(description = "즐겨찾기 여부", example = "true")
    private boolean isFavorite;
    
    @Schema(description = "메모", example = "대학교 친구")
    private String memo;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
    
    public static ContactResponse from(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .contactName(contact.getContactName())
                .contactPhoneNumber(contact.getContactPhoneNumber())
                .isRegistered(contact.isRegistered())
                .isVerified(contact.isVerified())
                .isFavorite(contact.isFavorite())
                .memo(contact.getMemo())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}