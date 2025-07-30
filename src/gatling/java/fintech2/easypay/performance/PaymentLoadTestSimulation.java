package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 결제 서비스 대용량 트래픽 부하 테스트
 * 
 * 테스트 시나리오:
 * 1. 사용자 로그인
 * 2. 결제 요청 (다양한 금액)
 * 3. 결제 상태 확인
 */
public class PaymentLoadTestSimulation extends Simulation {

    // HTTP 프로토콜 설정
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Performance Test");

    // 테스트 데이터 피더 - 실제 생성된 사용자 계정들 (단순화)
    private FeederBuilder<Object> userFeeder = listFeeder(List.of(
        Map.of("accountNumber", "VA73248887200"),
        Map.of("accountNumber", "VA4716305984"),
        Map.of("accountNumber", "VA87296566240"),
        Map.of("accountNumber", "VA6412667742"),
        Map.of("accountNumber", "VA4057790245"),
        Map.of("accountNumber", "VA52868263189"),
        Map.of("accountNumber", "VA52796358138"),
        Map.of("accountNumber", "VA26968382193"),
        Map.of("accountNumber", "VA26356435241"),
        Map.of("accountNumber", "VA7532721438")
    )).circular();
    
    // 로그인을 위한 패스워드 피더
    private FeederBuilder<Object> loginFeeder = listFeeder(List.of(
        Map.of("phoneNumber", "010-1111-1111", "password", "password123"),
        Map.of("phoneNumber", "010-2222-2222", "password", "password123"),
        Map.of("phoneNumber", "010-3333-3333", "password", "password123"),
        Map.of("phoneNumber", "010-4444-4444", "password", "password123"),
        Map.of("phoneNumber", "010-5555-5555", "password", "password123"),
        Map.of("phoneNumber", "010-6666-6666", "password", "password123"),
        Map.of("phoneNumber", "010-7777-7777", "password", "password123"),
        Map.of("phoneNumber", "010-8888-8888", "password", "password123"),
        Map.of("phoneNumber", "010-9999-9999", "password", "password123"),
        Map.of("phoneNumber", "010-1010-1010", "password", "password123")
    )).circular();

    // 결제 금액 피더 - 다양한 결제 시나리오
    private FeederBuilder<Object> paymentAmountFeeder = listFeeder(List.of(
        Map.of("amount", "5000", "merchantName", "스타벅스"),
        Map.of("amount", "15000", "merchantName", "맥도날드"),
        Map.of("amount", "30000", "merchantName", "CGV"),
        Map.of("amount", "50000", "merchantName", "올리브영"),
        Map.of("amount", "100000", "merchantName", "유니클로"),
        Map.of("amount", "200000", "merchantName", "Apple Store"),
        Map.of("amount", "500000", "merchantName", "현대백화점"),
        Map.of("amount", "1000000", "merchantName", "롯데호텔"),
        Map.of("amount", "2000000", "merchantName", "BMW"),
        Map.of("amount", "5000000", "merchantName", "루이비통")
    )).random();

    // 향상된 로그인 시나리오 - 토큰 재사용 및 캐싱
    private ChainBuilder loginChain = exec(
        feed(loginFeeder)
        .doIf(session -> !session.contains("accessToken") || 
               session.getString("accessToken") == null || 
               session.getString("accessToken").isEmpty()).then(
            exec(
                http("사용자 로그인")
                    .post("/auth/login")
                    .body(StringBody("""
                        {
                            "phoneNumber": "#{phoneNumber}",
                            "password": "#{password}"
                        }
                        """)).asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.accessToken").saveAs("accessToken"))
                    .check(jsonPath("$.accountNumber").optional().saveAs("accountNumber"))
            )
        )
    );

    // 결제 시나리오
    private ChainBuilder paymentChain = exec(
        feed(paymentAmountFeeder)
        .exec(
            http("결제 요청")
                .post("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    String merchantId = "MERCHANT_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                    return String.format("""
                        {
                            "merchantId": "%s",
                            "merchantName": "%s",
                            "amount": %s,
                            "memo": "Gatling 부하테스트 결제",
                            "paymentMethod": "BALANCE"
                        }
                        """, 
                        merchantId,
                        session.getString("merchantName"),
                        session.getString("amount")
                    );
                })).asJson()
                .check(status().in(200, 201))
                .check(jsonPath("$.paymentId").optional().saveAs("paymentId"))
        )
        .pause(1, 3) // 1-3초 대기
        .doIf(session -> session.contains("paymentId")).then(
            exec(
                http("결제 상태 확인")
                    .get("/payments/#{paymentId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
            )
        )
    );

    // 계좌 잔액 확인 시나리오
    private ChainBuilder balanceCheckChain = exec(
        http("계좌 잔액 조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
            .check(jsonPath("$.balance").saveAs("currentBalance"))
    );

    // 전체 결제 플로우
    private ScenarioBuilder paymentScenario = scenario("결제 부하 테스트")
        .exec(loginChain)
        .pause(1, 2)
        .exec(balanceCheckChain)
        .pause(1, 2)
        .repeat(3).on( // 사용자당 3번의 결제 시도
            exec(paymentChain)
            .pause(2, 5)
        );

    // 급증하는 트래픽 시나리오 (Black Friday 같은 상황)
    private ScenarioBuilder spikingTrafficScenario = scenario("급증 트래픽 테스트")
        .exec(loginChain)
        .pause(1)
        .exec(balanceCheckChain)
        .pause(1)
        .exec(paymentChain);

    // 지속적인 부하 시나리오
    private ScenarioBuilder sustainedLoadScenario = scenario("지속 부하 테스트")
        .exec(loginChain)
        .pause(2, 4)
        .forever().on(
            exec(paymentChain)
            .pace(Duration.ofSeconds(10)) // 10초마다 결제 시도
        );

    {
        setUp(
            // 시나리오 1: 일반적인 결제 부하 테스트
            paymentScenario.injectOpen(
                rampUsers(50).during(Duration.ofMinutes(2)), // 2분간 50명 증가
                constantUsersPerSec(20).during(Duration.ofMinutes(5)), // 5분간 초당 20명
                rampUsers(100).during(Duration.ofMinutes(3)) // 3분간 100명 추가 증가
            ).protocols(httpProtocol),

            // 시나리오 2: 급증하는 트래픽 (이벤트/세일 상황)
            spikingTrafficScenario.injectOpen(
                nothingFor(Duration.ofMinutes(1)), // 1분 대기
                atOnceUsers(200), // 200명이 동시에 접속
                rampUsers(300).during(Duration.ofSeconds(30)) // 30초간 300명 급증
            ).protocols(httpProtocol),

            // 시나리오 3: 지속적인 백그라운드 부하
            sustainedLoadScenario.injectOpen(
                constantUsersPerSec(5).during(Duration.ofMinutes(10)) // 10분간 초당 5명 지속
            ).protocols(httpProtocol)
        )
        .assertions(
            // 성능 기준 설정
            global().responseTime().max().lt(5000), // 최대 응답시간 5초 이하
            global().responseTime().mean().lt(1500), // 평균 응답시간 1.5초 이하
            global().successfulRequests().percent().gt(95.0), // 성공률 95% 이상
            forAll().failedRequests().count().is(0L) // 실패 요청 0개 (선택적)
        );
    }
}