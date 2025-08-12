# EasyPay - 간편 결제 및 송금 서비스

## 📋 프로젝트 개요

EasyPay는 사용자 친화적인 간편 결제 및 송금 서비스를 제공하는 핀테크 애플리케이션입니다. 다중 계좌 관리, 외부 은행 연동, 실시간 송금, 보안 PIN 인증 등의 기능을 제공합니다.

## 🚀 주요 기능

### 1. 사용자 인증 및 보안
- **JWT 인증**: stateless 토큰 기반 인증 시스템
- **PIN 보안**: 민감한 거래에 대한 추가 PIN 인증
- **패스워드 암호화**: BCrypt를 사용한 안전한 비밀번호 저장
- **로그인 이력 관리**: 사용자 접속 기록 추적
- **BCrypt 암호화**: 비밀번호 안전한 해싱
- **Spring Security**: 인증/인가 처리
- **API 보안**: 모든 API 인증 필요 (회원가입/로그인 제외)
- **SQL Injection 방지**: JPA/Hibernate 사용
- **감사 로깅**: 모든 거래 이력 추적

### 2. 계좌 관리
- **다중 계좌 지원**: 하나의 사용자가 여러 계좌 보유 가능
- **주 계좌 설정**: 기본 거래 계좌 지정
- **외부 계좌 연동**: 타 은행 계좌 등록 및 검증
- **가상 계좌**: 임시 거래용 가상 계좌 생성
- **실시간 잔액 조회**: 계좌별 잔액 및 거래 내역 확인

### 3. 금융 거래
- **입금/출금**: 계좌 입출금 처리
- **송금**: 사용자 간 실시간 송금
- **보안 송금**: PIN 검증을 통한 안전한 송금
- **거래 내역**: 상세한 거래 이력 관리
- **최근 송금 내역**: 자주 사용하는 수신자 관리

### 4. 결제 서비스
- **간편 결제**: 빠른 결제 처리
- **결제 취소/환불**: 유연한 결제 관리
- **결제 내역 조회**: 페이지네이션 지원 결제 이력

### 5. 알림 및 감사
- **실시간 알림**: 거래 알림 서비스
- **감사 로그**: 모든 중요 작업에 대한 감사 추적
- **이벤트 모니터링**: 시스템 이벤트 로깅

## 🛠 기술 스택

### Backend
- **Java 21** (Preview Features 활성화)
- **Spring Boot 3.5.3**
- **Spring Security**: 인증 및 권한 관리
- **Spring Data JPA**: 데이터베이스 ORM
- **JWT (JJWT)**: 토큰 기반 인증

### Database
- **H2 Database**: 개발 환경
- **PostgreSQL**: 운영 환경
- **Flyway**: 데이터베이스 마이그레이션 관리

### Caching
- **Caffeine Cache**: 로컬 캐싱
- **Spring Cache**: 캐시 추상화

### API Documentation
- **SpringDoc OpenAPI (Swagger)**: API 문서화

### Monitoring & Testing
- **Micrometer + Prometheus**: 메트릭 수집
- **Gatling**: 성능 테스트
- **JUnit 5**: 단위 테스트
- **Spring Security Test**: 보안 테스트

### DevOps
- **Docker**: 컨테이너화
- **Docker Compose**: 로컬 개발 환경
- **Nginx**: 리버스 프록시
- **Spring Boot DevTools**: 개발 생산성 향상

## 아키텍처 특징
- **모듈형 구조**: 도메인별 패키지 분리
- **비동기 처리**: 알림 및 외부 API 호출
- **Mock 서비스**: 개발/테스트용 외부 API 시뮬레이션

### 코드 스타일
- Java 코드 컨벤션 준수
- IntelliJ IDEA 기본 포맷터 사용
- 메서드는 한 가지 일만 수행
- 클래스는 단일 책임 원칙 준수


## 📁 프로젝트 구조

```
fintech2.easypay/
   account/          # 계좌 관리 (다중 계좌, 외부 계좌, 가상 계좌)
   audit/            # 감사 로그 및 알림 서비스
   auth/             # 인증/인가 (JWT, PIN, 사용자 관리)
   common/           # 공통 유틸리티 및 예외 처리
   config/           # 설정 클래스 (보안, 캐시, 스케줄링)
   external/         # 외부 API 연동 (은행 API)
   payment/          # 결제 처리 서비스
   transfer/         # 송금 서비스
```

## 🔒 API 엔드포인트

### 인증 (`/api/auth`)
- `POST /register` - 회원가입
- `POST /login` - 로그인
- `POST /refresh` - 토큰 갱신
- `GET /profile` - 프로필 조회
- `PUT /profile` - 프로필 수정

### 계좌 (`/api/accounts`)
- `GET /balance` - 잔액 조회
- `POST /deposit` - 입금
- `POST /withdraw` - 출금
- `GET /{accountNumber}/transactions` - 거래 내역

### 사용자 계좌 (`/api/user-accounts`)
- `GET /` - 전체 계좌 목록
- `POST /create` - 새 계좌 생성 (PIN 필요)
- `PUT /primary` - 주 계좌 변경
- `DELETE /{accountNumber}` - 계좌 비활성화 (PIN 필요)

### 외부 계좌 (`/api/external-accounts`)
- `GET /` - 외부 계좌 목록
- `POST /register` - 외부 계좌 등록
- `POST /{accountId}/re-verify` - 재검증
- `PUT /{accountId}/alias` - 별칭 변경

### 송금 (`/api/transfers`)
- `POST /` - 일반 송금
- `POST /secure` - 보안 송금 (PIN 검증)
- `GET /history` - 송금 내역
- `GET /recent` - 최근 송금처

### 결제 (`/api/payments`)
- `POST /` - 결제 처리
- `POST /{paymentId}/cancel` - 결제 취소
- `POST /{paymentId}/refund` - 환불
- `GET /` - 결제 내역

### PIN (`/api/pin`)
- `POST /register` - PIN 등록
- `POST /verify` - PIN 검증
- `PUT /change` - PIN 변경
- `GET /status` - PIN 상태 확인

## 🏃‍♂️ 실행 방법

### 개발 환경 설정

1. **요구사항**
   - Java 21 이상
   - Gradle 7.x 이상

2. **프로젝트 빌드**
   ```bash
   ./gradlew clean build
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

4. **프로파일별 실행**
   ```bash
   # 개발 환경 (기본값)
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   
   # 운영 환경
   ./gradlew bootRun --args='--spring.profiles.active=prod'
   ```

### Docker 실행

1. **Docker 이미지 빌드**
   ```bash
   docker build -t easypay:latest .
   ```

2. **Docker Compose로 실행**
   ```bash
   docker-compose up -d
   ```

## 🧪 테스트

### 단위 테스트
```bash
./gradlew test
```

### 성능 테스트 (Gatling)
```bash
./gradlew gatlingRun
```

### 코드 품질 검사
```bash
# SpotBugs 실행
./gradlew spotbugsMain

# PMD 실행
./gradlew pmdMain

# 의존성 취약점 검사
./gradlew dependencyCheckAnalyze
```

## 📊 모니터링

- **Actuator 엔드포인트**: `/actuator/*`
- **Prometheus 메트릭**: `/actuator/prometheus`
- **Health Check**: `/actuator/health`
- **API 문서 (Swagger)**: `/swagger-ui.html`

## 🔐 보안 기능

- JWT 토큰 기반 인증
- PIN 2차 인증
- BCrypt 패스워드 암호화
- CORS 설정
- SQL Injection 방지 (JPA 사용)
- XSS 방지
- CSRF 보호
- 민감 정보 로깅 방지

## 🤝 기여 방법
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Pull Request 가이드
1. feature 브랜치에서 작업 완료
2. develop 브랜치로 PR 생성
3. PR 템플릿에 따라 작성
4. 최소 1명 이상의 리뷰어 승인 필요
5. 모든 테스트 통과 확인
6. Squash and merge 사용

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
- **명명 규칙**: `feat/{작업자이름}/{작업내용}`
- **예시**:
   - `feat/songseongbeen/payment-transfer-implementation`
   - `feat/csh/250409-menu-error-fix`
   - `feat/john/user-authentication`
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
