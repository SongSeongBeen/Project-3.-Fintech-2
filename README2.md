<<<<<<< HEAD
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
=======
# EasyPay - 간편 송금 시스템

## 프로젝트 소개
EasyPay는 사용자 친화적인 간편송금 및 결제 서비스를 제공하는 핀테크 플랫폼입니다. 안전하고 빠른 금융 거래를 목표로 하며, 직관적인 인터페이스와 강력한 보안 기능을 제공합니다.

### 주요 기능
- 👤 **회원 관리**: 전화번호 기반 회원가입, JWT 토큰 인증
- 💳 **계좌 관리**: 계좌 생성, 조회, 잔액 관리
- 💸 **간편 결제**: 거래처 결제, 결제 내역 조회
- 💰 **송금 서비스**: 계좌 간 실시간 송금, 송금 상태 확인
- 📊 **거래 내역**: 송금/입금 내역 조회, 페이지네이션 지원
- 🔔 **알림 서비스**: 거래 알림, 비동기 알림 처리
- 🔐 **보안**: BCrypt 암호화, 거래 검증, 감사 로깅

## 기술 스택
>>>>>>> upstream/develop

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
<<<<<<< HEAD
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
=======
- **Build Tool**: Gradle 8.x
- **Database**: 
  - H2 Database (개발 환경)
  - MySQL 8.0 (운영 환경)

### Security & Authentication
- Spring Security 6.x
- JWT (jjwt 0.12.3)
- BCrypt Password Encoding

### Data Access
- Spring Data JPA
- Hibernate
- Flyway (DB 마이그레이션)

### API & Documentation
- RESTful API
- Springdoc OpenAPI (Swagger UI)

### 외부 연동
- MockBankingApiService (개발/테스트)
- MockPaymentGatewayService (개발/테스트)
- 실제 금융 API (운영 예정)

## 시작하기

### 사전 요구사항
- JDK 21 이상
- Gradle 8.0 이상
- Git

### 설치 및 실행

1. **프로젝트 클론**
```bash
git clone https://github.com/INNER-CIRCLE-ICD4/Project-3.-Fintech-2.git
cd Project-3.-Fintech-2
```

2. **의존성 설치 및 빌드**
```bash
./gradlew clean build
```

3. **애플리케이션 실행**
```bash
# 개발 환경 실행 (H2 Database)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경 실행 (MySQL 필요)
# 환경변수 설정 필요: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET
./gradlew bootRun --args='--spring.profiles.active=prod'
```

4. **테스트 실행**
```bash
./gradlew test
```

5. **H2 Console 접속 (개발 환경)**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (빈 값)
```

### API 문서
애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 브랜치 전략

본 프로젝트는 Git Flow 전략을 기반으로 한 브랜치 관리를 수행합니다.

### 📌 main (master)
- **용도**: 최종 배포 브랜치
- **특징**: 
  - 배포 이력 관리
  - 직접 commit 금지
  - release 브랜치에서만 merge
  - 태그를 통한 버전 관리

### 🚀 release
- **용도**: 운영 서버 배포를 위한 브랜치
- **특징**:
  - 운영 서버 CI/CD 연동 (수동 배포)
  - develop 브랜치에서 생성
  - 배포 준비 및 최종 테스트
  - hotfix 적용 대상

### 🔧 develop
- **용도**: 개발(테스트) 서버 배포를 위한 브랜치
- **특징**:
  - 개발 서버 CI/CD 연동 (자동 배포)
  - 모든 feature 브랜치가 merge되는 통합 브랜치
  - 다음 릴리즈를 위한 개발 진행

### ✨ feature
- **용도**: 개발을 위한 브랜치
- **명명 규칙**: `feature/{작업자이름}/{작업내용}`
- **예시**:
  - `feature/songseongbeen/payment-transfer-implementation`
  - `feature/csh/250409-menu-error-fix`
  - `feature/john/user-authentication`
- **작업 흐름**:
  1. develop 브랜치에서 생성
  2. 기능 개발 완료
  3. develop으로 Pull Request
  4. 코드 리뷰 후 merge

### 🔥 hotfix
- **용도**: 운영 환경 긴급 수정
- **명명 규칙**: `hotfix/{이슈번호}-{간단한설명}`
- **작업 흐름**:
  1. release 브랜치에서 생성
  2. 긴급 수정 적용
  3. release와 develop 브랜치에 모두 merge

## 개발 가이드

### 커밋 메시지 컨벤션
```
<type>: <subject>

<body>

<footer>
```

**Type 종류**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음)
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드 설정, 패키지 매니저 설정 등

**예시**
```
feat: 송금 API 구현

- 계좌 간 송금 기능 추가
- 송금 한도 체크 로직 구현
- 외부 뱅킹 API 연동

Resolves: #123
```

### 코드 스타일
- Java 코드 컨벤션 준수
- IntelliJ IDEA 기본 포맷터 사용
- 메서드는 한 가지 일만 수행
- 클래스는 단일 책임 원칙 준수

### Pull Request 가이드
1. feature 브랜치에서 작업 완료
2. develop 브랜치로 PR 생성
3. PR 템플릿에 따라 작성
4. 최소 1명 이상의 리뷰어 승인 필요
5. 모든 테스트 통과 확인
6. Squash and merge 사용

## 프로젝트 구조
```
src/main/java/fintech2/easypay/
├── account/           # 계좌 관리
├── member/            # 회원 관리  
├── payment/           # 결제 처리
├── transfer/          # 송금 처리
├── auth/              # 인증/인가
├── audit/             # 감사 로깅
├── common/            # 공통 컴포넌트
└── config/            # 설정 클래스
```

## 환경별 설정

### 개발 환경 (dev)
- **Database**: H2 In-Memory
- **특징**: H2 Console 활성화, 디버그 로깅, Mock 서비스

### 운영 환경 (prod)  
- **Database**: MySQL 8.0
- **특징**: HikariCP 커넥션 풀, 환경변수 설정, 최적화된 로깅

## 주요 API 엔드포인트

### 인증 관리
- `POST /api/auth/signup` - 회원가입 (전화번호, 비밀번호, 이름, 이메일)
- `POST /api/auth/signin` - 로그인 (전화번호, 비밀번호)
- `GET /api/members/me` - 내 정보 조회 (JWT 인증 필요)

### 계좌 관리
- `POST /api/accounts` - 계좌 생성
- `GET /api/accounts` - 내 계좌 목록 조회
- `GET /api/accounts/{accountNumber}` - 계좌 상세 조회
- `POST /api/accounts/{accountNumber}/deposit` - 입금
- `POST /api/accounts/{accountNumber}/withdraw` - 출금

### 결제
- `POST /api/payments` - 결제 처리
- `GET /api/payments/history` - 결제 내역 조회 (페이지네이션)
- `GET /api/payments/{transactionId}` - 결제 상세 조회

### 송금
- `POST /api/transfers` - 송금 요청
- `GET /api/transfers/{transactionId}` - 특정 거래 조회
- `GET /api/transfers/history` - 전체 거래 내역
- `GET /api/transfers/sent` - 송금 내역 조회
- `GET /api/transfers/received` - 입금 내역 조회

## 보안 고려사항
- **JWT 인증**: stateless 토큰 기반 인증 시스템
- **BCrypt 암호화**: 비밀번호 안전한 해싱
- **Spring Security**: 인증/인가 처리
- **API 보안**: 모든 API 인증 필요 (회원가입/로그인 제외)
- **SQL Injection 방지**: JPA/Hibernate 사용
- **감사 로깅**: 모든 거래 이력 추적

## 아키텍처 특징
- **모듈형 구조**: 도메인별 패키지 분리
- **비동기 처리**: 알림 및 외부 API 호출
- **Mock 서비스**: 개발/테스트용 외부 API 시뮬레이션

## 팀 정보
- **프로젝트명**: EasyPay
- **팀명**: INNER-CIRCLE-ICD4
- **개발 기간**: 2025.01 ~ 진행중
- **팀 구성**: 백엔드 개발자, 프론트엔드 개발자, 기획자

## 기여 가이드
1. 이슈 생성 또는 기존 이슈 확인
2. feature 브랜치 생성
3. 개발 및 테스트
4. PR 생성 및 리뷰 요청
5. 리뷰 반영 및 merge

## 라이선스
이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 문의사항
- 프로젝트 관련 문의는 Issues를 통해 등록해주세요.
- 긴급한 문의는 팀 Slack 채널을 이용해주세요.
>>>>>>> upstream/develop
