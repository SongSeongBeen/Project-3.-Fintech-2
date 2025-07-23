# EasyPay - 간편 송금 시스템

## 프로젝트 소개
EasyPay는 사용자 친화적인 간편송금 및 결제 서비스를 제공하는 핀테크 플랫폼입니다. 안전하고 빠른 금융 거래를 목표로 하며, 직관적인 인터페이스와 강력한 보안 기능을 제공합니다.

### 주요 기능
- 👤 **회원 관리**: 전화번호 기반 회원가입, JWT 토큰 인증
- 💳 **계좌 관리**: 계좌 생성, 조회, 잔액 관리
- 💸 **간편 결제**: 가맹점 결제, 결제 내역 조회
- 💰 **송금 서비스**: 계좌 간 실시간 송금, 송금 상태 확인
- 📊 **거래 내역**: 송금/입금 내역 조회, 페이지네이션 지원
- 🔔 **알림 서비스**: 거래 알림, 비동기 알림 처리
- 🔐 **보안**: BCrypt 암호화, 거래 검증, 감사 로깅

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
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
Project-3.-Fintech-2/
├── src/
│   ├── main/
│   │   ├── java/fintech2/easypay/
│   │   │   ├── account/        # 계좌 관리 모듈
│   │   │   ├── audit/          # 감사 로깅 모듈
│   │   │   ├── auth/           # 인증/인가 모듈
│   │   │   ├── common/         # 공통 유틸리티
│   │   │   ├── config/         # 설정 클래스
│   │   │   ├── member/         # 회원 관리 모듈
│   │   │   ├── payment/        # 결제 처리 모듈
│   │   │   └── transfer/       # 송금 처리 모듈
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/                   # 테스트 코드
├── gradle/                     # Gradle 래퍼
├── scripts/                    # 유틸리티 스크립트
├── .gitignore
├── .gitattributes
├── build.gradle.kts           # 빌드 설정
├── settings.gradle.kts        # 프로젝트 설정
└── README.md                  # 프로젝트 문서

```

## 환경별 설정

### 개발 환경 (dev)
- **Database**: H2 In-Memory
- **Port**: 8080
- **DDL**: create-drop (자동 생성/삭제)
- **특징**:
  - 디버그 로깅 활성화
  - H2 Console 활성화 (/h2-console)
  - Mock 외부 서비스 사용
  - SQL 쿼리 로깅

### 운영 환경 (prod)
- **Database**: MySQL 8.0
- **Port**: 8080
- **DDL**: validate (스키마 검증만)
- **특징**:
  - 최적화된 로깅 (INFO 레벨)
  - HikariCP 커넥션 풀 설정
  - 환경변수 기반 설정 (DB_HOST, DB_USERNAME 등)
  - JWT 시크릿 외부 주입

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

### 모듈 구조
- **account**: 계좌 관리 (Account, AccountService)
- **member**: 회원 관리 (Member, MemberService)
- **payment**: 결제 처리 (Payment, PaymentService)
- **transfer**: 송금 처리 (Transfer, TransferService)
- **auth**: 인증/인가 (JWT, CustomUserDetails)
- **audit**: 감사 로깅 (AuditLog, Notification)
- **common**: 공통 컴포넌트 (ApiResponse, BaseEntity)

### 비동기 처리
- 알림 서비스 비동기 처리
- 송금 상태 확인 스케줄링
- @Async 기반 백그라운드 작업

### Mock 서비스
- **MockBankingApiService**: 외부 은행 API 시뮬레이션
- **MockPaymentGatewayService**: 결제 게이트웨이 시뮬레이션
- 다양한 성공/실패 시나리오 제공

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