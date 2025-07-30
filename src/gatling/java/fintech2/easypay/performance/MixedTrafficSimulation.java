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
 * 실제 서비스 환경을 시뮬레이션하는 혼합 트래픽 테스트
 * 
 * 실제 서비스에서는 결제, 송금, 조회가 동시에 발생하므로
 * 이를 종합적으로 테스트하는 시나리오
 */
public class MixedTrafficSimulation extends Simulation {

    // HTTP 프로토콜 설정
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Mixed Traffic Test")
        .inferHtmlResources()
        .silentResources(); // 정적 리소스 로깅 생략

    // 사용자 계정 피더
    private FeederBuilder<Object> userFeeder = listFeeder(List.of(
        Map.of("phoneNumber", "010-1111-1111", "password", "password123", "userType", "heavy"), // 헤비 유저
        Map.of("phoneNumber", "010-2222-2222", "password", "password123", "userType", "normal"), // 일반 유저  
        Map.of("phoneNumber", "010-3333-3333", "password", "password123", "userType", "light"), // 라이트 유저
        Map.of("phoneNumber", "010-4444-4444", "password", "password123", "userType", "heavy"),
        Map.of("phoneNumber", "010-5555-5555", "password", "password123", "userType", "normal"),
        Map.of("phoneNumber", "010-6666-6666", "password", "password123", "userType", "light"),
        Map.of("phoneNumber", "010-7777-7777", "password", "password123", "userType", "normal"),
        Map.of("phoneNumber", "010-8888-8888", "password", "password123", "userType", "heavy"),
        Map.of("phoneNumber", "010-9999-9999", "password", "password123", "userType", "normal"),
        Map.of("phoneNumber", "010-1010-1010", "password", "password123", "userType", "light")
    )).circular();

    // 공통 로그인 체인
    private ChainBuilder loginChain = exec(
        feed(userFeeder)
        .exec(
            http("로그인")
                .post("/auth/login")
                .body(StringBody("""
                    {
                        "phoneNumber": "#{phoneNumber}",
                        "password": "#{password}"
                    }
                    """)).asJson()
                .check(status().is(200))
                .check(jsonPath("$.accessToken").saveAs("accessToken"))
        )
        .pause(1, 2)
    );

    // 헤비 유저 행동 패턴 (결제 + 송금 + 조회 모두 활발)
    private ChainBuilder heavyUserBehavior = exec(
        // 잔액 확인
        http("헤비유저-잔액조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    )
    .pause(1, 2)
    .repeat(2, "paymentCount").on(
        exec(
            http("헤비유저-결제")
                .post("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    int amount = ThreadLocalRandom.current().nextInt(10000, 500000);
                    String merchantId = "MERCHANT_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                    return String.format("""
                        {
                            "merchantId": "%s",
                            "merchantName": "헤비유저 쇼핑몰",
                            "amount": %d,
                            "memo": "헤비유저 결제",
                            "paymentMethod": "BALANCE"
                        }
                        """, merchantId, amount);
                })).asJson()
                .check(status().in(200, 201))
        )
        .pause(2, 4)
    )
    .repeat(3, "transferCount").on(
        exec(
            http("헤비유저-송금")
                .post("/transfers")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    int amount = ThreadLocalRandom.current().nextInt(50000, 1000000);
                    // 실제 수신자 계좌 사용 대신 네트워크 내 계좌로 송금
                    // 실제 수신자 계좌 사용
                    String[] receiverAccounts = {"VA52868263189", "VA52796358138", "VA26968382193", "VA26356435241", "VA7532721438"};
                    String receiverAccount = receiverAccounts[ThreadLocalRandom.current().nextInt(receiverAccounts.length)];
                    return String.format("""
                        {
                            "receiverAccountNumber": "%s",
                            "amount": %d,
                            "memo": "헤비유저 송금"
                        }
                        """, receiverAccount, amount);
                })).asJson()
                .check(status().in(200, 201))
        )
        .pause(3, 6)
    )
    .exec(
        http("헤비유저-거래내역조회")
            .get("/transfers/history")
            .header("Authorization", "Bearer #{accessToken}")
            .queryParam("page", "0")
            .queryParam("size", "20")
            .check(status().is(200))
    );

    // 일반 유저 행동 패턴 (결제 위주, 가끔 송금)
    private ChainBuilder normalUserBehavior = exec(
        http("일반유저-잔액조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    )
    .pause(2, 3)
    .repeat(1, "paymentCount").on(
        exec(
            http("일반유저-결제")
                .post("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    int amount = ThreadLocalRandom.current().nextInt(5000, 100000);
                    String merchantId = "MERCHANT_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                    return String.format("""
                        {
                            "merchantId": "%s",
                            "merchantName": "일반 온라인몰",
                            "amount": %d,
                            "memo": "일반유저 결제",
                            "paymentMethod": "BALANCE"
                        }
                        """, merchantId, amount);
                })).asJson()
                .check(status().in(200, 201))
        )
        .pause(3, 7)
    )
    // 50% 확률로 송금 실행
    .randomSwitch().on(
        Choice.withWeight(50.0, 
            exec(
                http("일반유저-송금")
                    .post("/transfers")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody(session -> {
                        int amount = ThreadLocalRandom.current().nextInt(10000, 200000);
                        // 실제 수신자 계좌 사용 대신 네트워크 내 계좌로 송금
                    // 실제 수신자 계좌 사용
                    String[] receiverAccounts = {"VA52868263189", "VA52796358138", "VA26968382193", "VA26356435241", "VA7532721438"};
                    String receiverAccount = receiverAccounts[ThreadLocalRandom.current().nextInt(receiverAccounts.length)];
                        return String.format("""
                            {
                                "receiverAccountNumber": "%s",
                                "amount": %d,
                                "memo": "일반유저 송금"
                            }
                            """, receiverAccount, amount);
                    })).asJson()
                    .check(status().in(200, 201))
            )
            .pause(2, 4)
        ),
        Choice.withWeight(50.0, exec(pause(2, 4))) // 50%는 송금 안함
    );

    // 라이트 유저 행동 패턴 (조회 위주, 가끔 소액 결제)
    private ChainBuilder lightUserBehavior = exec(
        http("라이트유저-잔액조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    )
    .pause(3, 5)
    .exec(
        http("라이트유저-거래내역조회")
            .get("/transfers/history")
            .header("Authorization", "Bearer #{accessToken}")
            .queryParam("page", "0")
            .queryParam("size", "5")
            .check(status().is(200))
    )
    .pause(4, 8)
    // 30% 확률로만 결제 실행
    .randomSwitch().on(
        Choice.withWeight(30.0,
            exec(
                http("라이트유저-소액결제")
                    .post("/payments")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody(session -> {
                        int amount = ThreadLocalRandom.current().nextInt(3000, 30000);
                        String merchantId = "MERCHANT_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                        return String.format("""
                            {
                                "merchantId": "%s",
                                "merchantName": "편의점",
                                "amount": %d,
                                "memo": "라이트유저 소액결제",
                                "paymentMethod": "BALANCE"
                            }
                            """, merchantId, amount);
                    })).asJson()
                    .check(status().in(200, 201))
            )
        ),
        Choice.withWeight(70.0, exec(pause(2, 3))) // 70%는 결제 안함
    );

    // 사용자 타입별 시나리오 분기
    private ChainBuilder userBehaviorSwitch = 
        doSwitch("#{userType}").on(
            Choice.withKey("heavy", exec(heavyUserBehavior)),
            Choice.withKey("normal", exec(normalUserBehavior)),
            Choice.withKey("light", exec(lightUserBehavior))
        );

    // 혼합 트래픽 시나리오
    private ScenarioBuilder mixedTrafficScenario = scenario("혼합 트래픽 시뮬레이션")
        .exec(loginChain)
        .exec(userBehaviorSwitch);

    // API 상태 체크 시나리오 (모니터링)
    private ScenarioBuilder healthCheckScenario = scenario("시스템 상태 체크")
        .exec(
            http("Health Check")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(10); // 10초마다 헬스체크

    {
        setUp(
            // 메인 혼합 트래픽
            mixedTrafficScenario.injectOpen(
                // 서서히 증가하는 트래픽 패턴 (실제 서비스와 유사)
                nothingFor(Duration.ofSeconds(10)),
                rampUsers(20).during(Duration.ofMinutes(1)), // 점진적 증가
                constantUsersPerSec(5).during(Duration.ofMinutes(2)), // 안정적 트래픽
                rampUsers(50).during(Duration.ofMinutes(2)), // 피크 타임 시뮬레이션
                constantUsersPerSec(15).during(Duration.ofMinutes(3)), // 피크 유지
                rampUsers(10).during(Duration.ofMinutes(1)) // 감소
            ).protocols(httpProtocol),

            // 지속적인 헬스체크
            healthCheckScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(10))
            ).protocols(httpProtocol)
        )
        .assertions(
            // 전체 성능 기준
            global().responseTime().max().lt(10000), // 최대 응답시간 10초
            global().responseTime().mean().lt(2500), // 평균 응답시간 2.5초
            global().responseTime().percentile3().lt(5000), // 95 퍼센타일 5초
            global().successfulRequests().percent().gt(92.0), // 성공률 92% 이상
            
            // 사용자 타입별 성능 기준
            details("헤비유저-결제").responseTime().mean().lt(3000),
            details("일반유저-결제").responseTime().mean().lt(2000),
            details("라이트유저-소액결제").responseTime().mean().lt(1500),
            
            // 조회 API 성능 기준
            details("잔액조회").responseTime().max().lt(2000),
            details("거래내역조회").responseTime().max().lt(3000),
            
            // 헬스체크는 항상 성공해야 함
            details("Health Check").failedRequests().count().is(0L)
        );
    }
}