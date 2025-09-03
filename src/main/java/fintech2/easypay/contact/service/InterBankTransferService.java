package fintech2.easypay.contact.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 타행 송금 서비스
 * 
 * 타행 송금 방안:
 * 
 * 1. 오픈뱅킹 API 연동
 *    - 금융결제원 오픈뱅킹 API 활용
 *    - 실시간 타행 이체 가능
 *    - 수수료: 건당 400~500원
 *    - 장점: 실시간 처리, 안정적
 *    - 단점: API 연동 복잡도, 인증 절차 필요
 * 
 * 2. 가상계좌 활용 방안
 *    - 수신자별 고유 가상계좌 발급
 *    - 송금자가 가상계좌로 입금
 *    - 입금 확인 후 수신자 계좌로 자동 이체
 *    - 장점: 구현 간단, 추적 용이
 *    - 단점: 실시간성 부족, 사용자 경험 복잡
 * 
 * 3. 펌뱅킹(CMS) 서비스
 *    - 대량 이체 처리에 적합
 *    - 배치 처리 방식 (일 1~3회)
 *    - 수수료 저렴 (건당 100~200원)
 *    - 장점: 비용 효율적
 *    - 단점: 실시간 불가능
 * 
 * 4. 제휴 은행 우선 연동
 *    - 주요 은행과 직접 제휴
 *    - 제휴 은행 간 수수료 우대
 *    - 비제휴 은행은 오픈뱅킹 활용
 *    - 장점: 수수료 절감, 서비스 차별화
 *    - 단점: 제휴 협상 필요
 * 
 * 5. 하이브리드 방식 (추천)
 *    - 소액: 오픈뱅킹 API (실시간)
 *    - 대액: 가상계좌 + 본인인증
 *    - 정기 송금: CMS 활용
 *    - 제휴 은행: 우대 수수료
 * 
 * 구현 우선순위:
 * 1단계: 오픈뱅킹 API 연동 (기본 타행 송금)
 * 2단계: 가상계좌 시스템 구축 (대액 송금)
 * 3단계: 제휴 은행 확대
 * 4단계: CMS 서비스 도입 (B2B, 급여이체 등)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterBankTransferService {
    
    /**
     * 타행 송금 가능 여부 확인
     */
    public boolean isInterBankTransferAvailable(String bankCode) {
        // 현재 지원되는 은행 코드 확인
        return isSupportedBank(bankCode);
    }
    
    /**
     * 타행 송금 수수료 계산
     */
    public BigDecimal calculateInterBankFee(String senderBank, String receiverBank, BigDecimal amount) {
        // 동일 은행
        if (senderBank.equals(receiverBank)) {
            return BigDecimal.ZERO;
        }
        
        // 제휴 은행
        if (isPartnerBank(receiverBank)) {
            return BigDecimal.valueOf(200); // 우대 수수료
        }
        
        // 일반 타행
        if (amount.compareTo(BigDecimal.valueOf(50000)) <= 0) {
            return BigDecimal.valueOf(500); // 5만원 이하
        } else {
            return BigDecimal.valueOf(1000); // 5만원 초과
        }
    }
    
    /**
     * 타행 송금 예상 소요 시간
     */
    public String getEstimatedTransferTime(String bankCode, BigDecimal amount) {
        // 오픈뱅킹 실시간 이체
        if (amount.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
            return "즉시 (1분 이내)";
        }
        
        // 대액 이체 (가상계좌)
        if (amount.compareTo(BigDecimal.valueOf(10000000)) <= 0) {
            return "10분 이내";
        }
        
        // 초대액 이체 (추가 인증)
        return "30분 ~ 1시간";
    }
    
    private boolean isSupportedBank(String bankCode) {
        // 지원 은행 목록
        String[] supportedBanks = {
            "004", // KB국민은행
            "088", // 신한은행
            "020", // 우리은행
            "081", // 하나은행
            "003", // 기업은행
            "011", // 농협은행
            "023", // SC제일은행
            "032", // 부산은행
            "071", // 우체국
            "090"  // 카카오뱅크
        };
        
        for (String bank : supportedBanks) {
            if (bank.equals(bankCode)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPartnerBank(String bankCode) {
        // 제휴 은행 (예시)
        String[] partnerBanks = {
            "088", // 신한은행
            "090"  // 카카오뱅크
        };
        
        for (String bank : partnerBanks) {
            if (bank.equals(bankCode)) {
                return true;
            }
        }
        return false;
    }
}