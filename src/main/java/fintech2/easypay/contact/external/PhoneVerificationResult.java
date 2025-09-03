package fintech2.easypay.contact.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneVerificationResult {
    private boolean verified;
    private String phoneNumber;
    private String name;
    private String carrier;
    private String message;
}