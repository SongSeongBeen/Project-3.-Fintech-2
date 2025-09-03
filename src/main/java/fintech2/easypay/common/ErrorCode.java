package fintech2.easypay.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 오류
    INVALID_REQUEST("E001", "잘못된 요청입니다."),
    UNAUTHORIZED("E002", "인증이 필요합니다."),
    FORBIDDEN("E003", "권한이 없습니다."),
    NOT_FOUND("E004", "요청한 리소스를 찾을 수 없습니다."),
    
    // 회원 관련 오류
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다."),
    PHONE_NUMBER_ALREADY_EXISTS("M002", "이미 존재하는 휴대폰 번호입니다."),
    INVALID_CREDENTIALS("M003", "아이디 또는 비밀번호가 잘못되었습니다."),
    LOGIN_FAILED("M004", "로그인에 실패했습니다."),
    MEMBER_ALREADY_DELETED("M005", "이미 탈퇴한 회원입니다."),
    
    // 계좌 관련 오류
    ACCOUNT_NOT_FOUND("A001", "계좌를 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE("A002", "잔액이 부족합니다."),
    INVALID_ACCOUNT_NUMBER("A003", "잘못된 계좌번호입니다."),
    ACCOUNT_CREATION_FAILED("A004", "계좌 생성에 실패했습니다."),
    
    // 거래 관련 오류
    TRANSACTION_FAILED("T001", "거래 처리에 실패했습니다."),
    INVALID_AMOUNT("T002", "유효하지 않은 금액입니다."),
    SAME_ACCOUNT_TRANSFER("T003", "같은 계좌로는 송금할 수 없습니다."),
    TRANSACTION_NOT_FOUND("T004", "거래 내역을 찾을 수 없습니다."),
    
    // 결제 관련 오류
    PAYMENT_FAILED("P001", "결제에 실패했습니다."),
    PAYMENT_NOT_FOUND("P002", "결제 내역을 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED("P003", "이미 처리된 결제입니다."),
    REFUND_FAILED("P004", "환불에 실패했습니다."),
    PAYMENT_CANCEL_FAILED("P005", "결제 취소에 실패했습니다."),
    PAYMENT_REFUND_FAILED("P006", "결제 환불에 실패했습니다."),
    PAYMENT_CANNOT_BE_CANCELLED("P007", "취소할 수 없는 결제입니다."),
    PAYMENT_CANNOT_BE_REFUNDED("P008", "환불할 수 없는 결제입니다."),
    INVALID_REFUND_AMOUNT("P009", "유효하지 않은 환불 금액입니다."),
    
    // PIN 인증 관련 오류
    PIN_NOT_SET("PIN001", "PIN이 설정되지 않았습니다."),
    PIN_ALREADY_SET("PIN002", "이미 PIN이 설정되어 있습니다."),
    INVALID_PIN("PIN003", "PIN이 일치하지 않습니다."),
    PIN_LOCKED("PIN004", "PIN이 잠금되었습니다."),
    INVALID_PIN_FORMAT("PIN005", "PIN은 6자리 숫자여야 합니다."),
    INVALID_PIN_SESSION("PIN006", "PIN 인증 세션이 유효하지 않습니다."),
    PIN_SESSION_EXPIRED("PIN007", "PIN 인증 세션이 만료되었습니다."),
    
    // 예약 송금 관련 오류
    SCHEDULE_NOT_FOUND("SCH001", "예약 송금을 찾을 수 없습니다."),
    SCHEDULE_ALREADY_CANCELLED("SCH002", "이미 취소된 예약 송금입니다."),
    SCHEDULE_ALREADY_COMPLETED("SCH003", "이미 완료된 예약 송금입니다."),
    INVALID_SCHEDULE_TIME("SCH004", "유효하지 않은 예약 시간입니다."),
    SCHEDULE_LIMIT_EXCEEDED("SCH005", "예약 송금 개수 제한을 초과했습니다."),
    INVALID_REPEAT_CYCLE("SCH006", "유효하지 않은 반복 주기입니다."),
    DUPLICATE_TRANSFER("DUP001", "중복된 송금 요청입니다."),
    INVALID_PHONE_NUMBER("VER001", "유효하지 않은 전화번호입니다."),
    LIMIT_EXCEEDED("LIM001", "한도를 초과했습니다."),
    INVALID_STATUS("ST001", "유효하지 않은 상태입니다."),
    
    // 시스템 오류
    INTERNAL_SERVER_ERROR("S001", "내부 서버 오류가 발생했습니다."),
    DATABASE_ERROR("S002", "데이터베이스 오류가 발생했습니다."),
    EXTERNAL_API_ERROR("S003", "외부 API 호출에 실패했습니다.");
    
    private final String code;
    private final String message;
}
