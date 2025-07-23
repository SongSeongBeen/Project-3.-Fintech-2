# EasyPay - 간편결제 시스템

## 프로젝트 소개
EasyPay는 사용자 친화적인 간편결제 및 송금 서비스를 제공하는 핀테크 플랫폼입니다.

### 주요 기능
- 👤 회원 관리 (가입/로그인/인증)
- 💳 계좌 관리 (생성/조회/입출금)
- 💸 간편 결제
- 💰 송금 서비스
- 📊 거래 내역 조회
- 🔔 실시간 알림

## 기술 스택
- **Backend**: Spring Boot 3.5.3, Java 21
- **Database**: H2 (개발), PostgreSQL (운영)
- **Security**: Spring Security, JWT
- **Build**: Gradle
- **API Documentation**: Swagger/OpenAPI

## 시작하기

### 사전 요구사항
- JDK 21 이상
- Gradle 8.0 이상

### 설치 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/INNER-CIRCLE-ICD4/Project-3.-Fintech-2.git
cd Project-3.-Fintech-2

# 빌드
./gradlew build

# 실행 (개발 환경)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 실행 (운영 환경)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### API 문서
애플리케이션 실행 후 http://localhost:8080/swagger-ui.html 에서 API 문서를 확인할 수 있습니다.

## 브랜치 전략

### main (master)
- 최종 Branch
- 배포 이력 관리 용
- 직접 commit 금지

### release
- 운영 서버 배포를 위한 Branch
- 운영 서버와 CI/CD 연동 (수동 배포)
- develop 브랜치에서 생성

### develop
- 개발(테스트) 서버 배포를 위한 Branch
- 개발(테스트) 서버 CI/CD 연동 (자동 배포)
- feature 브랜치들이 merge되는 통합 브랜치

### feature
- 개발을 위한 Branch
- develop을 기준으로 branch 생성
- 명명 규칙: `feature/{작업자이름}/{날짜}-{작업내용}`
  - 예시 1: `feature/csh/250409-menu-error-fix`
  - 예시 2: `feature/ssb/250123-payment-api`

### hotfix
- 운영 환경 긴급 수정을 위한 Branch
- release 브랜치에서 생성
- 수정 후 release와 develop에 모두 merge

## 개발 가이드

### 브랜치 작업 흐름
1. develop에서 feature 브랜치 생성
2. 기능 개발 및 테스트
3. develop으로 Pull Request 생성
4. 코드 리뷰 및 merge
5. 개발 서버에서 테스트
6. release 브랜치로 merge
7. 운영 배포 후 main으로 merge

### 커밋 메시지 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드 업무 수정, 패키지 매니저 수정
```

## 프로젝트 구조
```
src/
├── main/
│   ├── java/fintech2/easypay/
│   │   ├── account/        # 계좌 관리
│   │   ├── audit/          # 감사 로깅
│   │   ├── auth/           # 인증/인가
│   │   ├── common/         # 공통 유틸리티
│   │   ├── config/         # 설정
│   │   ├── member/         # 회원 관리
│   │   ├── payment/        # 결제 처리
│   │   └── transfer/       # 송금 처리
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/                   # 테스트 코드
```

## 환경 설정

### 개발 환경 (dev)
- H2 In-Memory Database
- 디버그 로깅 활성화
- Mock 외부 API 서비스

### 운영 환경 (prod)
- PostgreSQL Database
- 성능 최적화 설정
- 실제 외부 API 연동

## 팀 정보
- **프로젝트명**: EasyPay
- **팀명**: INNER-CIRCLE-ICD4
- **개발 기간**: 2025.01 ~ 진행중

## 라이선스
이 프로젝트는 MIT 라이선스 하에 있습니다.