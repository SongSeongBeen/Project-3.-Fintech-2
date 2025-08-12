package fintech2.easypay.transfer.service.flow.context;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import fintech2.easypay.auth.entity.User;
import fintech2.easypay.account.entity.Account;
import fintech2.easypay.transfer.entity.Transfer;
import fintech2.easypay.transfer.client.BankingApiResponse;

/**
 * 송금 처리 컨텍스트 (불변 객체)
 * 송금 처리 과정에서 필요한 모든 정보를 담는 불변 컨테이너
 */
@Getter
@Builder
public class TransferContext {
    // 기본 정보
    private final String senderPhone;
    private final String transactionId;
    
    // 참여자 정보
    private final User sender;
    private final User receiver;
    private final Account senderAccount;
    private final Account receiverAccount;
    
    // 송금 정보
    private final BigDecimal amount;
    private final String memo;
    
    // 진행 중에 채워질 수 있는 값들
    private final Transfer transfer;
    private final BankingApiResponse apiResponse;
    
    /**
     * Transfer 엔티티를 추가한 새로운 컨텍스트 생성
     */
    public TransferContext withTransfer(Transfer t) {
        return TransferContext.builder()
                .senderPhone(senderPhone)
                .transactionId(transactionId)
                .sender(sender)
                .receiver(receiver)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .amount(amount)
                .memo(memo)
                .transfer(t)
                .apiResponse(apiResponse)
                .build();
    }
    
    /**
     * API 응답을 추가한 새로운 컨텍스트 생성
     */
    public TransferContext withApiResponse(BankingApiResponse res) {
        return TransferContext.builder()
                .senderPhone(senderPhone)
                .transactionId(transactionId)
                .sender(sender)
                .receiver(receiver)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .amount(amount)
                .memo(memo)
                .transfer(transfer)
                .apiResponse(res)
                .build();
    }
    
    /**
     * 컨텍스트가 유효한지 확인
     */
    public boolean isValid() {
        return sender != null && 
               receiver != null && 
               senderAccount != null && 
               receiverAccount != null && 
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0 &&
               transactionId != null && !transactionId.isEmpty();
    }
    
    /**
     * 송금이 완료되었는지 확인
     */
    public boolean isTransferCompleted() {
        return transfer != null && transfer.isCompleted();
    }
    
    /**
     * 외부 API 호출이 성공했는지 확인
     */
    public boolean isApiCallSuccessful() {
        return apiResponse != null && 
               apiResponse.getStatus() == fintech2.easypay.transfer.client.BankingApiStatus.SUCCESS;
    }
}