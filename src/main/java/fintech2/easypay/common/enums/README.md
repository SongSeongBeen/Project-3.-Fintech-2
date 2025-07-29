# Enums

이 폴더는 애플리케이션에서 사용되는 모든 열거형(Enum) 클래스들을 포함합니다.

## 포함된 Enum들

- `AccountStatus`: 계좌 상태 (ACTIVE, INACTIVE, LOCKED)
- `AuditResult`: 감사 결과 (SUCCESS, FAIL, ERROR, WARNING)
- `LoginResult`: 로그인 결과 (SUCCESS, FAILED, ACCOUNT_NOT_FOUND, ACCOUNT_LOCKED)
- `TransactionStatus`: 거래 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
- `TransactionType`: 거래 유형 (DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND)
- `UserStatus`: 사용자 상태 (ACTIVE, INACTIVE, WITHDRAWN, SUSPENDED)

## 사용법

```java
import fintech2.easypay.common.enums.AccountStatus;
import fintech2.easypay.common.enums.TransactionType;

// 사용 예시
AccountStatus status = AccountStatus.ACTIVE;
TransactionType type = TransactionType.DEPOSIT;
``` 