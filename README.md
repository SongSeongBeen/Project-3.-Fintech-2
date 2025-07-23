# EasyPay - 간편결제 시스템

## 프로젝트 소개
EasyPay는 사용자 친화적인 간편결제 및 송금 서비스를 제공하는 핀테크 플랫폼입니다. 안전하고 빠른 금융 거래를 목표로 하며, 직관적인 인터페이스와 강력한 보안 기능을 제공합니다.

### 주요 기능
- 👤 **회원 관리**: 회원가입, 로그인, JWT 기반 인증
- 💳 **계좌 관리**: 계좌 생성, 조회, 입출금 처리
- 💸 **간편 결제**: QR코드 기반 결제, 결제 승인/취소
- 💰 **송금 서비스**: 계좌 간 실시간 송금
- 📊 **거래 내역**: 상세 거래 내역 조회 및 필터링
- 🔔 **실시간 알림**: 거래 알림, 보안 알림
- 🔐 **보안**: 암호화, 거래 한도 관리, 이상 거래 탐지

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Build Tool**: Gradle 8.x
- **Database**: 
  - H2 Database (개발 환경)
  - PostgreSQL (운영 환경)

### Security & Authentication
- Spring Security
- JWT (JSON Web Token)
- BCrypt Password Encoding

### API & Documentation
- RESTful API
- Swagger/OpenAPI 3.0
- Spring REST Docs

### 외부 연동
- Mock Banking API Service (개발)
- Mock Payment Gateway Service (개발)
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
# 개발 환경 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경 실행
./gradlew bootRun --args='--spring.profiles.active=prod'
```

4. **테스트 실행**
```bash
./gradlew test
```

### API 문서
애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

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
- **특징**:
  - 디버그 로깅 활성화
  - Mock 외부 서비스 사용
  - 개발자 친화적 에러 메시지

### 운영 환경 (prod)
- **Database**: PostgreSQL
- **Port**: 8080
- **특징**:
  - 최적화된 로깅
  - 실제 외부 API 연동
  - 보안 설정 강화

## 주요 API 엔드포인트

### 회원 관리
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/signin` - 로그인
- `GET /api/members/me` - 내 정보 조회

### 계좌 관리
- `POST /api/accounts` - 계좌 생성
- `GET /api/accounts` - 계좌 목록 조회
- `GET /api/accounts/{accountNumber}` - 계좌 상세 조회

### 결제
- `POST /api/payments` - 결제 요청
- `GET /api/payments/{transactionId}` - 결제 상태 조회
- `POST /api/payments/{transactionId}/cancel` - 결제 취소

### 송금
- `POST /api/transfers` - 송금 요청
- `GET /api/transfers/{transactionId}` - 송금 상태 조회
- `GET /api/transfers/history` - 송금 내역 조회

## 보안 고려사항
- JWT 기반 stateless 인증
- 모든 API는 인증 필요 (일부 공개 API 제외)
- 암호는 BCrypt로 해싱
- HTTPS 통신 (운영 환경)
- SQL Injection 방지 (JPA 사용)
- XSS 방지 처리

## 성능 최적화
- 데이터베이스 인덱싱
- 페이지네이션 적용
- 비동기 처리 (송금, 알림)
- 캐싱 전략 (예정)

## 모니터링 및 로깅
- SLF4J + Logback
- 환경별 로그 레벨 설정
- 거래 감사 로깅
- 에러 추적 시스템 (예정)

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