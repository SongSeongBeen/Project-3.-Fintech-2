package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 간단한 성능 테스트 - 기본 기능 검증
 */
public class QuickTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Quick Test");

    // 실제 생성된 테스트 사용자들
    private FeederBuilder<Object> userFeeder = listFeeder(List.of(
        Map.of("phoneNumber", "010-1111-1111", "password", "password123"),
        Map.of("phoneNumber", "010-2222-2222", "password", "password123"),
        Map.of("phoneNumber", "010-3333-3333", "password", "password123"),
        Map.of("phoneNumber", "010-4444-4444", "password", "password123"),
        Map.of("phoneNumber", "010-5555-5555", "password", "password123")
    )).circular();

    // 간단한 로그인 테스트 시나리오
    private ScenarioBuilder loginTestScenario = scenario("간단한 로그인 테스트")
        .feed(userFeeder)
        .exec(
            http("로그인 요청")
                .post("/auth/login")
                .body(StringBody("""
                    {
                        "phoneNumber": "#{phoneNumber}",
                        "password": "#{password}"
                    }
                    """)).asJson()
                .check(status().in(200, 400, 401)) // 성공하거나 인증 실패 허용
                .check(jsonPath("$.accessToken").optional().saveAs("accessToken"))
        )
        .pause(1)
        .doIf(session -> session.contains("accessToken")).then(
            exec(
                http("계좌 잔액 조회")
                    .get("/accounts/balance")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().in(200, 401, 403))
            )
        );

    // 헬스체크 시나리오
    private ScenarioBuilder healthCheckScenario = scenario("시스템 상태 확인")
        .exec(
            http("Health Check")
                .get("/actuator/health")
                .check(status().is(200))
                .check(jsonPath("$.status").is("UP"))
        );

    {
        setUp(
            // 간단한 로그인 테스트
            loginTestScenario.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10)), // 10초간 5명
                constantUsersPerSec(2).during(Duration.ofSeconds(30)) // 30초간 초당 2명
            ).protocols(httpProtocol),

            // 헬스체크는 지속적으로
            healthCheckScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(1)) // 1분간 초당 1번
            ).protocols(httpProtocol)
        )
        .assertions(
            // 관대한 성능 기준 (첫 테스트이므로)
            global().responseTime().max().lt(10000), // 최대 10초
            global().responseTime().mean().lt(3000), // 평균 3초
            global().successfulRequests().percent().gt(80.0), // 성공률 80% 이상
            details("Health Check").failedRequests().count().is(0L) // 헬스체크는 100% 성공
        );
    }
}