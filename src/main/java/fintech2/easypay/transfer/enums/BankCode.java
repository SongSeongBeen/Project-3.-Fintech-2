package fintech2.easypay.transfer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 은행 코드 열거형
 * 국내 은행 및 자사 서비스 식별 코드
 */
@Getter
@RequiredArgsConstructor
public enum BankCode {
    // 자사 서비스
    EASYPAY("999", "이지페이", true),
    
    // 국내 은행
    KB("004", "KB국민은행", false),
    IBK("003", "IBK기업은행", false),
    HANA("081", "하나은행", false),
    SHINHAN("088", "신한은행", false),
    NH("011", "NH농협은행", false),
    WOORI("020", "우리은행", false),
    SC("023", "SC제일은행", false),
    CITI("027", "한국씨티은행", false),
    KDB("002", "KDB산업은행", false),
    BUSAN("032", "부산은행", false),
    DAEGU("031", "대구은행", false),
    GWANGJU("034", "광주은행", false),
    JEJU("035", "제주은행", false),
    JEONBUK("037", "전북은행", false),
    KYONGNAM("039", "경남은행", false),
    SUHYUP("007", "수협은행", false),
    KBANK("089", "케이뱅크", false),
    KAKAO("090", "카카오뱅크", false),
    TOSS("092", "토스뱅크", false);
    
    private final String code;
    private final String name;
    private final boolean internal;  // EASYPAY 내부 계좌 여부
    
    /**
     * 은행 코드로 BankCode 찾기
     */
    public static BankCode fromCode(String code) {
        for (BankCode bankCode : values()) {
            if (bankCode.code.equals(code)) {
                return bankCode;
            }
        }
        throw new IllegalArgumentException("Invalid bank code: " + code);
    }
    
    /**
     * EASYPAY 내부 계좌인지 확인
     */
    public boolean isInternal() {
        return internal;
    }
    
    /**
     * 외부 은행 계좌인지 확인
     */
    public boolean isExternal() {
        return !internal;
    }
}