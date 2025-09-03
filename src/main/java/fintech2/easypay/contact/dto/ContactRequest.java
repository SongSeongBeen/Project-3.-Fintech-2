package fintech2.easypay.contact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "연락처 추가/수정 요청")
public class ContactRequest {
    
    @NotBlank(message = "연락처 이름은 필수입니다.")
    @Size(max = 50, message = "연락처 이름은 50자 이하여야 합니다.")
    @Schema(description = "연락처 이름", example = "김친구")
    private String contactName;
    
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    @Schema(description = "연락처 전화번호", example = "01087654321")
    private String contactPhoneNumber;
    
    @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
    @Schema(description = "메모", example = "대학교 친구")
    private String memo;
}