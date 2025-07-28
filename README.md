# 🏦 EasyPay - 금융 서비스 API

안전하고 편리한 가상계좌 기반 금융 서비스 플랫폼

## 📋 목차
- [구현 기능](#-구현-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [API 가이드](#-api-가이드)
- [JWT 인증 시스템](#-jwt-인증-시스템)
- [잔액 처리 아키텍처](#-잔액-처리-아키텍처)
- [알람 시스템](#-알람-시스템)
- [테스트 가이드](#-테스트-가이드)
- [개발 가이드](#-개발-가이드)

## 🚀 구현 기능

### 1. 회원가입/인증 시스템
- **회원가입**: 휴대폰 번호 중복 체크, 비밀번호 암호화(BCrypt), 가상계좌 자동 생성
- **로그인**: JWT 토큰 발급, 5회 실패 시 30분 계정 잠금
- **보안**: JWT 기반 인증, 로그인 이력 기록, 계정 잠금 관리
- **토큰 관리**: Access Token (1시간), Refresh Token (30일), 자동 갱신

### 2. 가상계좌 및 잔액 관리
- **가상계좌 생성**: "VA" + 8자리 숫자 + 2자리 체크섬 형태
- **잔액 조회**: JWT 인증 기반 본인 계좌 조회
- **잔액 증감**: Pessimistic Lock, 잔액 부족 검증, 거래내역 자동 기록
- **거래내역**: 모든 거래 추적 및 조회 가능

### 3. 알람/감사 로그 시스템
- **감사 로그**: 모든 중요 비즈니스 이벤트 기록
- **자동 알람**: 에러/경고/중요 이벤트 시 실시간 알림
- **사용자/관리자 구분**: 거래내역(사용자) vs 시스템에러(관리자)
- **확장성**: SMTP, Slack 연동 준비 완료

### 4. 프론트엔드 기능
- **반응형 웹**: 모바일 친화적 UI/UX
- **실시간 알림**: 잔액 변동, 로그인 성공/실패 알림
- **필터링**: 알림 카테고리별 필터링 (전체/잔액/로그인/시스템)
- **페이징**: 알림 목록 10개씩 페이징 처리

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: H2 (인메모리, 개발용)
- **ORM**: JPA/Hibernate 6.6.18
- **Security**: Spring Security + JWT (JJWT 0.12.5)
- **Build Tool**: Gradle 8.x

### 주요 라이브러리
- **JWT**: io.jsonwebtoken:jjwt-api:0.12.5
- **Password Encoding**: BCrypt
- **Database Migration**: Flyway
- **Testing**: JUnit 5 + Mockito

### Frontend
- **HTML5, CSS3, JavaScript (Vanilla)**
- **반응형 디자인**
- **JWT 토큰 기반 인증**

## 📁 프로젝트 구조

```
src/main/java/fintech2/easypay/
├── auth/                    # 인증 관련
│   ├── entity/             # User, RefreshToken, LoginHistory
│   ├── service/            # AuthService, TokenService, JwtService, LoginHistoryService
│   ├── repository/         # UserRepository, RefreshTokenRepository, LoginHistoryRepository
│   ├── dto/                # RegisterRequest, LoginRequest, AuthResponse
│   ├── controller/         # AuthController
│   └── filter/             # JwtAuthenticationFilter
├── account/                # 계좌 관련
│   ├── entity/             # VirtualAccount, AccountBalance, TransactionHistory
│   ├── service/            # AccountService, BalanceService, TransferService
│   ├── repository/         # VirtualAccountRepository, AccountBalanceRepository, TransactionHistoryRepository
│   ├── controller/         # AccountController, TransferController
│   └── dto/                # AccountInfoResponse, BalanceResponse, TransactionResponse
├── audit/                  # 감사 관련
│   ├── entity/             # AuditLog
│   ├── service/            # AuditLogService, AlarmService
│   ├── repository/         # AuditLogRepository
│   └── controller/         # AlarmController
├── common/                 # 공통
│   ├── exception/          # GlobalExceptionHandler, AuthException, AccountNotFoundException
│   └── enums/              # UserStatus, AccountStatus, TransactionStatus, TransactionType
└── config/                 # 설정
    ├── SecurityConfig      # Spring Security 설정
    └── SchedulingConfig    # 스케줄링 설정

src/main/resources/static/  # 프론트엔드
├── index.html             # 로그인 페이지 (기본)
├── register.html          # 회원가입 페이지
├── main.html             # 메인 페이지 (송금/결제/잔액 버튼)
├── balance.html          # 잔액조회 페이지
├── alarm.html            # 알람 페이지
├── js/
│   ├── auth.js           # 인증 관련 JS
│   ├── main.js           # 메인 페이지 JS
│   ├── balance.js        # 잔액조회 JS
│   ├── alarm.js          # 알람 JS
│   └── api.js            # API 공통 JS
└── css/
    ├── common.css        # 공통 스타일
    └── login.css         # 로그인 스타일
```

## 📡 API 가이드

### 인증 API

#### 회원가입
```http
POST /auth/register
Content-Type: application/json

{
    "phoneNumber": "010-1234-5678",
    "password": "password123",
    "name": "홍길동"
}
```

**응답 (성공 201)**
```json
{
    "message": "회원가입이 완료되었습니다",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accountNumber": "VA1234567890",
    "userName": "홍길동"
}
```

#### 로그인
```http
POST /auth/login
Content-Type: application/json

{
    "phoneNumber": "010-1234-5678",
    "password": "password123"
}
```

**응답 (성공 200)**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accountNumber": "VA1234567890",
    "userName": "홍길동"
}
```

### 계좌 API

#### 잔액 조회
```http
GET /accounts/{accountNumber}/balance
Authorization: Bearer {accessToken}
```

**응답 (성공 200)**
```json
{
    "accountNumber": "VA1234567890",
    "balance": 50000.00,
    "currency": "KRW"
}
```

#### 거래내역 조회
```http
GET /accounts/{accountNumber}/transactions
Authorization: Bearer {accessToken}
```

**응답 (성공 200)**
```json
[
    {
        "id": 1,
        "accountNumber": "VA1234567890",
        "transactionType": "DEPOSIT",
        "amount": 10000.00,
        "balanceBefore": 40000.00,
        "balanceAfter": 50000.00,
        "description": "테스트 입금",
        "status": "COMPLETED",
        "createdAt": "2024-01-15T10:30:00"
    }
]
```

### 알람 API

#### 알림 개수 조회
```http
GET /api/alarms/count
Authorization: Bearer {accessToken}
```

**응답 (성공 200)**
```json
{
    "count": 3,
    "success": true
}
```

#### 알림 목록 조회
```http
GET /api/alarms/list?category=all
Authorization: Bearer {accessToken}
```

**응답 (성공 200)**
```json
{
    "alarms": [
        {
            "id": 1234567890,
            "type": "BALANCE_CHANGE",
            "message": "계좌 VA1234567890의 잔액이 증가되었습니다. 금액: 50000원, 잔액: 150000원",
            "timestamp": "2024-01-15T10:30:00",
            "level": "info",
            "category": "balance"
        }
    ],
    "success": true
}
```

## 🔐 JWT 인증 시스템

### JWT 토큰 구조
- **Access Token**: 1시간 유효 (API 호출용)
- **Refresh Token**: 30일 유효 (토큰 갱신용, DB 저장)

### JWT Secret Key 설정
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890123456789012345678901234567890
  expiration:
    access: 3600000       # Access Token 유효 시간 (1시간)
    refresh: 2592000000   # Refresh Token 유효 시간 (30일)
```

**⚠️ 운영 환경에서는 반드시 강력한 Secret Key로 변경해야 합니다!**

## 🏦 잔액 처리 아키텍처

### 중앙화된 잔액 서비스 (BalanceService)

```java
@Service
public class BalanceService {
    
    @Transactional
    public BalanceChangeResult increase(String accountNumber, BigDecimal amount, 
                                      TransactionType transactionType, String description, String referenceId) {
        // 입금 처리
    }
    
    @Transactional
    public BalanceChangeResult decrease(String accountNumber, BigDecimal amount, 
                                      TransactionType transactionType, String description, String referenceId) {
        // 출금 처리
    }
    
    private BalanceChangeResult changeBalance(...) {
        // 1. Pessimistic Lock으로 동시성 제어
        Optional<AccountBalance> accountOpt = accountBalanceRepository.findByIdWithLock(accountNumber);
        
        // 2. 잔액 검증
        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            // 잔액 부족 알림 발송
            alarmService.sendInsufficientBalanceAlert(accountNumber, userId, currentBalance, requiredAmount);
            throw new InsufficientBalanceException(...);
        }
        
        // 3. 잔액 업데이트 (Lock으로 보호됨)
        account.setBalance(balanceAfter);
        accountBalanceRepository.save(account);
        
        // 4. 거래내역 기록
        transactionHistoryRepository.save(transaction);
        
        // 5. 잔액 변동 알림 발송
        alarmService.sendBalanceChangeAlert(accountNumber, userId, changeType, amount, balanceAfter);
    }
}
```

## 🔔 알람 시스템

### 사용자 vs 관리자 알림 구분

#### 사용자 알림 (거래내역, 잔액 변동 등)
```java
// 잔액 변동 알림
alarmService.sendBalanceChangeAlert(accountNumber, userId, "입금", "50000", "150000");

// 잔액 부족 알림
alarmService.sendInsufficientBalanceAlert(accountNumber, userId, "30000", "100000");

// 계정 잠금 알림
alarmService.sendAccountLockAlert(phoneNumber, userId, "5회 연속 로그인 실패");
```

#### 관리자 알림 (시스템 에러, 보안 이슈 등)
```java
// 시스템 에러 알림
alarmService.sendSystemAlert("DATABASE", "데이터베이스 연결 오류", exception);

// 보안 이슈 알림 (자동)
// - 계정 잠금 시 관리자에게 보안 이슈 알림
// - 로그인 실패 시 관리자에게 보안 이슈 알림
```

### 알림 유형별 처리

#### 사용자 알림 유형
- **BALANCE_CHANGE**: 잔액 변동 알림
- **INSUFFICIENT_BALANCE**: 잔액 부족 알림
- **ACCOUNT_LOCK**: 계정 잠금 알림
- **LOGIN_FAILURE**: 로그인 실패 알림
- **LOGIN_SUCCESS**: 로그인 성공 알림

#### 관리자 알림 유형
- **SYSTEM_ERROR**: 시스템 에러 알림
- **SECURITY_ISSUE**: 보안 이슈 알림
- **DATABASE_ERROR**: 데이터베이스 에러 알림
- **NETWORK_ERROR**: 네트워크 에러 알림


## 🧪 테스트 가이드

### 1. 서버 실행
```bash
./gradlew bootRun
```

### 2. 웹 브라우저 테스트
1. http://localhost:8090 접속
2. 회원가입 진행 (010-1234-5678, password123, 홍길동)
3. 로그인 진행
4. 메인 페이지에서 "잔액조회" 클릭
5. 테스트 입금/출금으로 기능 확인

### 3. 테스트 계정 정보
- **휴대폰**: 010-1234-5678
- **비밀번호**: 123456
- **계좌번호**: VA1234567890
- **초기 잔액**: 1,000,000원

### 4. 주요 테스트 시나리오

#### 잔액 부족 알림 테스트
1. 잔액 조회 페이지 접속
2. 출금 금액을 현재 잔액보다 크게 설정
3. "테스트 출금" 버튼 클릭
4. 알림 페이지에서 "잔액" 필터 클릭
5. 잔액 부족 알림 확인

#### 알림 필터링 테스트
1. 알림 페이지 접속
2. "전체", "잔액", "로그인", "시스템" 버튼 클릭
3. 각 필터별로 올바른 알림이 표시되는지 확인
4. 페이징 기능 확인 (10개씩 표시)

#### 알림 개수 테스트
1. 메인 페이지에서 알림 개수 확인
2. 알림 페이지 방문 후 메인 페이지로 돌아가기
3. 알림 개수가 정확히 업데이트되는지 확인

## 💻 개발 가이드

### JWT 인증 사용법

#### 기본 사용법
```java
@RestController
public class MyController {
    
    @GetMapping("/my-api")
    public ResponseEntity<MyResponse> myApi(
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // 사용자 정보 추출
        Long userId = userPrincipal.getId();
        String phoneNumber = userPrincipal.getPhoneNumber();
        String accountNumber = userPrincipal.getAccountNumber();
        
        // 비즈니스 로직 구현
        MyResponse response = myService.doSomething(userId);
        return ResponseEntity.ok(response);
    }
}
```

### BalanceService 사용법 (송금/결제 담당자)

#### 기본 사용법
```java
@Service
public class TransferService {
    
    private final BalanceService balanceService;
    
    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        // 1. 잔액 확인
        if (!balanceService.hasSufficientBalance(fromAccount, amount)) {
            throw new InsufficientBalanceException("잔액 부족");
        }
        
        // 2. 출금 처리
        balanceService.decrease(fromAccount, amount, TransactionType.TRANSFER, "송금 출금", transferId);
        
        // 3. 입금 처리
        balanceService.increase(toAccount, amount, TransactionType.TRANSFER, "송금 입금", transferId);
    }
}
```

### 알림 시스템 사용법

#### 사용자 알림 발송
```java
// 잔액 변동 알림
alarmService.sendBalanceChangeAlert(accountNumber, userId, "입금", "50000", "150000");

// 잔액 부족 알림
alarmService.sendInsufficientBalanceAlert(accountNumber, userId, "30000", "100000");
```

#### 관리자 알림 발송
```java
// 시스템 에러 알림
alarmService.sendSystemAlert("DATABASE", "데이터베이스 연결 오류", exception);

// 보안 이슈 알림
alarmService.sendAdminNotification("SECURITY_ISSUE", "계정 잠금: " + phoneNumber, null);
```

## 🔐 보안 고려사항

- JWT 토큰 기반 인증
- BCrypt 비밀번호 암호화
- 계정 잠금 (5회 실패 시 30분)
- 로그인 이력 추적
- 모든 중요 액션 감사로그 기록
- CORS 설정
- XSS/CSRF 방어
- Pessimistic Lock으로 동시성 제어

## 📊 현재 실행 상태

### 애플리케이션 상태
- ✅ **실행 중**: 포트 8090에서 정상 실행
- ✅ **데이터베이스**: H2 콘솔 접근 가능 (`/h2-console`)
- ✅ **API 엔드포인트**: 모든 API 구현 완료

### 사용 가능한 API
1. `POST /auth/register` - 회원가입 ✅
2. `POST /auth/login` - 로그인 ✅
3. `POST /auth/refresh` - 토큰 갱신 ✅
4. `POST /auth/logout` - 로그아웃 ✅
5. `GET /accounts/{accountNumber}/balance` - 잔액 조회 ✅
6. `GET /accounts/{accountNumber}/transactions` - 거래내역 조회 ✅
7. `GET /api/alarms/count` - 알림 개수 조회 ✅
8. `GET /api/alarms/list` - 알림 목록 조회 ✅


---