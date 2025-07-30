package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 통합 플로우 성능 테스트
 * 실제 사용자 여정을 시뮬레이션: 회원가입 → 로그인 → 잔액 조회 → 결제/송금 시도
 */
public class IntegratedFlowTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Integrated Test")
        .connectionHeader("keep-alive");

    // 새로운 사용자 생성을 위한 피더
    private FeederBuilder<Object> newUserFeeder = listFeeder(List.of(
        Map.of("phoneNumber", "010-1001-1001", "password", "password123", "name", "성능테스트유저1"),
        Map.of("phoneNumber", "010-1002-1002", "password", "password123", "name", "성능테스트유저2"),
        Map.of("phoneNumber", "010-1003-1003", "password", "password123", "name", "성능테스트유저3"),
        Map.of("phoneNumber", "010-1004-1004", "password", "password123", "name", "성능테스트유저4"),
        Map.of("phoneNumber", "010-1005-1005", "password", "password123", "name", "성능테스트유저5"),
        Map.of("phoneNumber", "010-1006-1006", "password", "password123", "name", "성능테스트유저6"),
        Map.of("phoneNumber", "010-1007-1007", "password", "password123", "name", "성능테스트유저7"),
        Map.of("phoneNumber", "010-1008-1008", "password", "password123", "name", "성능테스트유저8"),
        Map.of("phoneNumber", "010-1009-1009", "password", "password123", "name", "성능테스트유저9"),
        Map.of("phoneNumber", "010-1010-1010", "password", "password123", "name", "성능테스트유저10")
    )).circular();

    // 완전한 신규 사용자 여정 시나리오
    private ChainBuilder newUserJourneyChain = exec(
        feed(newUserFeeder)
        .exec(
            http("회원가입")
                .post("/auth/register")
                .body(StringBody("""
                    {
                        "phoneNumber": "#{phoneNumber}",
                        "password": "#{password}",
                        "name": "#{name}"
                    }
                    """)).asJson()
                .check(status().in(200, 201, 400)) // 201 Created와 중복 가입시 400도 허용
                .check(jsonPath("$.accessToken").optional().saveAs("accessToken"))
                .check(jsonPath("$.accountNumber").optional().saveAs("accountNumber"))
        )
        .pause(1, 2)
        .doIf(session -> !session.contains("accessToken")).then(
            // 회원가입 실패시 로그인 시도
            exec(
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
                    .check(jsonPath("$.accountNumber").saveAs("accountNumber"))
            )
        )
        .pause(1, 2)
        .exec(
            http("잔액 조회")
                .get("/accounts/balance")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(jsonPath("$.balance").saveAs("currentBalance"))
        )
        .pause(2, 4)
        // 잔액 충전 시도
        .exec(
            http("잔액 충전")
                .post("/accounts/deposit")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody("""
                    {
                        "amount": 500000
                    }
                    """)).asJson()
                .check(status().in(200, 400, 500)) // 에러도 허용
        )
        .pause(1, 2)
        // 카드 결제 시도 (잔액 부족 회피)
        .exec(
            http("카드 결제 시도")
                .post("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody("""
                    {
                        "merchantId": "TEST_MERCHANT_#{phoneNumber}",
                        "merchantName": "테스트 가맹점",
                        "amount": 10000,
                        "paymentMethod": "CARD",
                        "memo": "Gatling 성능 테스트 결제",
                        "cardNumber": "1234567890123456",
                        "cardExpiryDate": "12/25",
                        "cardCvv": "123"
                    }
                    """)).asJson()
                .check(status().in(200, 400, 402, 500)) // 에러도 허용
        )
        .pause(1, 3)
        // 송금 시도
        .exec(
            http("송금 시도")
                .post("/transfers")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody("""
                    {
                        "receiverAccountNumber": "#{accountNumber}",
                        "amount": 5000,
                        "memo": "Gatling 성능 테스트 송금"
                    }
                    """)).asJson()
                .check(status().in(200, 400, 402)) // 잔액 부족시 402도 허용
        )
    );

    // 기존 사용자 로그인 후 조회 중심 시나리오
    private ChainBuilder existingUserChain = exec(
        http("로그인")
            .post("/auth/login")
            .body(StringBody("""
                {
                    "phoneNumber": "010-1111-1111",
                    "password": "password123"
                }
                """)).asJson()
            .check(status().in(200, 401)) // 실패도 허용
            .check(jsonPath("$.accessToken").optional().saveAs("accessToken"))
    )
    .doIf(session -> session.contains("accessToken")).then(
        exec(
            http("잔액 조회")
                .get("/accounts/balance")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
        )
        .pause(2, 5)
        .exec(
            http("결제 내역 조회")
                .get("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 204)) // 내역이 없어도 허용
        )
        .pause(1, 3)
        .exec(
            http("송금 내역 조회")
                .get("/transfers/history")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 204)) // 내역이 없어도 허용
        )
    );

    // 헬스체크 시나리오
    private ScenarioBuilder healthCheckScenario = scenario("시스템 상태 모니터링")
        .exec(
            http("헬스체크")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(10);

    // 신규 사용자 플로우
    private ScenarioBuilder newUserScenario = scenario("신규 사용자 통합 플로우")
        .exec(newUserJourneyChain);

    // 기존 사용자 플로우
    private ScenarioBuilder existingUserScenario = scenario("기존 사용자 조회 플로우")
        .exec(existingUserChain);

    {
        setUp(
            // 신규 사용자 시나리오 (실제 서비스 신규 가입자 패턴)
            newUserScenario.injectOpen(
                rampUsers(20).during(Duration.ofMinutes(2)), // 2분간 20명 신규 가입
                constantUsersPerSec(5).during(Duration.ofMinutes(3)), // 3분간 초당 5명 신규 가입
                rampUsers(30).during(Duration.ofMinutes(1)) // 1분간 30명 추가
            ).protocols(httpProtocol),

            // 기존 사용자 시나리오 (일반적인 앱 사용 패턴)
            existingUserScenario.injectOpen(
                nothingFor(Duration.ofMinutes(1)),
                rampUsers(50).during(Duration.ofMinutes(2)), // 2분간 50명 로그인
                constantUsersPerSec(10).during(Duration.ofMinutes(3)) // 3분간 초당 10명 활동
            ).protocols(httpProtocol),

            // 백그라운드 모니터링
            healthCheckScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(6))
            ).protocols(httpProtocol)
        )
        .assertions(
            // 전체 성능 목표 (현실적으로 설정)
            global().responseTime().max().lt(15000), // 최대 15초 (결제/송금은 복잡한 처리)
            global().responseTime().mean().lt(3000), // 평균 3초
            global().successfulRequests().percent().gt(75.0), // 성공률 75% (잔액 부족 등으로 일부 실패 예상)

            // API별 성능 기준
            details("회원가입").responseTime().max().lt(5000),
            details("로그인").responseTime().max().lt(3000),
            details("잔액 조회").responseTime().max().lt(2000),
            
            // 헬스체크는 항상 성공해야 함
            details("헬스체크").failedRequests().count().is(0L),
            
            // 로그인은 높은 성공률 유지
            details("로그인").successfulRequests().percent().gt(90.0)
        );
    }
}