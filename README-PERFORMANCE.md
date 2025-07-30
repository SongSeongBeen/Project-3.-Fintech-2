# 🚀 Fintech 서비스 성능 테스트 가이드

## 개요
EasyPay 핀테크 서비스의 결제/송금 API에 대한 대용량 트래픽 성능 테스트 환경입니다.

## 🛠 구성된 테스트 시나리오

### 1. PaymentLoadTestSimulation
**결제 서비스 전용 부하 테스트**
- **테스트 대상**: 결제 API (`/payments`)
- **시나리오**:
  - 일반 결제 부하 테스트 (2분간 50명 → 5분간 초당 20명 → 3분간 100명 추가)
  - 급증 트래픽 테스트 (200명 동시 접속 + 30초간 300명 급증)
  - 지속적인 백그라운드 부하 (10분간 초당 5명)
- **성능 기준**:
  - 최대 응답시간: 5초 이하
  - 평균 응답시간: 1.5초 이하
  - 성공률: 95% 이상

### 2. TransferLoadTestSimulation
**송금 서비스 전용 부하 테스트**
- **테스트 대상**: 송금 API (`/transfers`)
- **시나리오**:
  - 일반 송금 부하 테스트
  - 대량 송금 테스트 (급여이체 시간대 시뮬레이션)
  - 연속 송금 테스트 (헤비 유저)
  - 송금 조회 중심 테스트
- **성능 기준**:
  - 최대 응답시간: 8초 이하
  - 평균 응답시간: 2초 이하
  - 성공률: 90% 이상

### 3. MixedTrafficSimulation ⭐ **추천**
**실제 서비스 환경 시뮬레이션**
- **테스트 대상**: 전체 API (결제 + 송금 + 조회)
- **사용자 타입별 행동 패턴**:
  - **헤비 유저**: 결제 2회 + 송금 3회 + 적극적 조회
  - **일반 유저**: 결제 1회 + 50% 확률 송금
  - **라이트 유저**: 조회 위주 + 30% 확률 소액결제
- **트래픽 패턴**: 점진적 증가 → 피크 → 감소 (실제 서비스와 유사)

## 🏃‍♂️ 실행 방법

### 1. 기본 준비
```bash
# 1. 애플리케이션 실행
./gradlew bootRun

# 2. 별도 터미널에서 성능 테스트 실행
```

### 2. 개별 시나리오 실행
```bash
# 결제 전용 테스트
./gradlew gatlingRun-fintech2.easypay.performance.PaymentLoadTestSimulation

# 송금 전용 테스트  
./gradlew gatlingRun-fintech2.easypay.performance.TransferLoadTestSimulation

# 혼합 트래픽 테스트 (추천)
./gradlew gatlingRun-fintech2.easypay.performance.MixedTrafficSimulation
```

### 3. 전체 시나리오 실행
```bash
# 모든 성능 테스트 실행
./gradlew gatlingRun
```

## 📊 결과 확인

### 1. 리포트 위치
```
build/reports/gatling/
├── paymentloadtestsimulation-20231129120000/
│   ├── index.html          # 📈 상세 리포트
│   ├── global_stats.json   # 📊 통계 데이터
│   └── simulation.log      # 🔍 상세 로그
├── transferloadtestsimulation-20231129120500/
└── mixedtrafficsimulation-20231129121000/
```

### 2. 리포트 해석
- **Response Time**: 응답 시간 분포 및 추이
- **Requests per Second**: 초당 처리 요청 수
- **Active Users**: 동시 접속자 수 변화
- **Success/Failure Rate**: 성공률 및 에러율

## ⚙️ 설정 커스터마이징

### 1. 테스트 강도 조절
`src/gatling/java/*/performance/*Simulation.java` 파일에서:
```java
// 사용자 수 조절
rampUsers(50).during(Duration.ofMinutes(2))  // 50명 → 원하는 수

// 지속 시간 조절
constantUsersPerSec(20).during(Duration.ofMinutes(5))  // 5분 → 원하는 시간

// 성능 기준 조절
global().responseTime().max().lt(5000)  // 5초 → 원하는 기준
```

### 2. 테스트 데이터 수정
각 시뮬레이션 파일의 Feeder 섹션에서:
```java
private FeederBuilder<String> userFeeder = listFeeder(
    map("phoneNumber", "010-1111-1111").put("password", "password123"),
    // 추가 테스트 계정들...
);
```

### 3. 서버 주소 변경
```java
private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:8080")  // 대상 서버 주소 변경
```

## 🎯 권장 테스트 시나리오

### 1. 개발 단계
```bash
# 가벼운 테스트로 시작
./gradlew gatlingRun-fintech2.easypay.performance.MixedTrafficSimulation
```

### 2. 스테이징 단계
```bash
# 각 서비스별 개별 테스트
./gradlew gatlingRun-fintech2.easypay.performance.PaymentLoadTestSimulation
./gradlew gatlingRun-fintech2.easypay.performance.TransferLoadTestSimulation
```

### 3. 운영 배포 전
```bash
# 모든 시나리오 종합 테스트
./gradlew gatlingRun
```

## 🚨 주의사항

1. **테스트 환경**: 운영 환경에서 직접 실행하지 마세요
2. **데이터베이스**: H2 인메모리 DB 사용 시 대용량 테스트 제한 있음
3. **네트워크**: 로컬 환경에서는 네트워크 병목이 발생하지 않을 수 있음
4. **JVM 설정**: 대용량 테스트 시 힙 메모리 증설 필요할 수 있음

## 🔧 트러블슈팅

### 1. 메모리 부족 에러
```bash
# JVM 힙 메모리 증설
export JAVA_OPTS="-Xmx2g -Xms1g"
./gradlew gatlingRun
```

### 2. 연결 타임아웃
애플리케이션의 `application.yml`에서:
```yaml
server:
  tomcat:
    threads:
      max: 200        # 스레드 풀 증설
    connection-timeout: 20000
```

### 3. 데이터베이스 성능
PostgreSQL 사용 시:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 커넥션 풀 크기 증설
      connection-timeout: 30000
```

## 📈 성능 최적화 가이드

1. **응답 시간 개선**: 데이터베이스 인덱스, 쿼리 최적화
2. **처리량 증대**: 스레드 풀, 커넥션 풀 크기 조정
3. **에러율 감소**: 예외 처리, 재시도 로직 강화
4. **확장성 확보**: 로드 밸런서, 캐시 시스템 도입

## 🎪 고급 시나리오 예시

### 블랙 프라이데이 시뮬레이션
```java
setUp(
    scenario("블랙프라이데이").injectOpen(
        nothingFor(Duration.ofMinutes(1)),
        atOnceUsers(1000),  // 동시 1000명 폭증
        rampUsers(2000).during(Duration.ofMinutes(5))  // 5분간 2000명 추가
    )
);
```

### 점심시간 급여이체 시뮬레이션
```java
setUp(
    scenario("급여이체시간").injectOpen(
        rampUsers(500).during(Duration.ofMinutes(10)),  // 10분간 500명 증가
        constantUsersPerSec(50).during(Duration.ofMinutes(30))  // 30분간 피크 유지
    )
);
```