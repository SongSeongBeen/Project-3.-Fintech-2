package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 매우 간단한 성능 테스트 - 낮은 부하로 시작
 */
public class SimpleTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Simple Test");

    // 하나의 사용자만 사용
    private FeederBuilder<Object> userFeeder = listFeeder(List.of(
        Map.of("phoneNumber", "010-1111-1111", "password", "password123")
    )).circular();

    // 헬스체크 시나리오
    private ScenarioBuilder healthCheckScenario = scenario("헬스체크 테스트")
        .exec(
            http("헬스체크")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(5);

    // 단일 로그인 시나리오
    private ScenarioBuilder singleLoginScenario = scenario("단일 로그인 테스트")
        .feed(userFeeder)
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
        .pause(10);

    {
        setUp(
            // 매우 낮은 부하로 시작
            healthCheckScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(2))
            ).protocols(httpProtocol),

            singleLoginScenario.injectOpen(
                rampUsers(5).during(Duration.ofMinutes(1)), // 1분간 5명만
                constantUsersPerSec(2).during(Duration.ofMinutes(1)) // 초당 2명만
            ).protocols(httpProtocol)
        )
        .assertions(
            global().responseTime().max().lt(5000),
            global().successfulRequests().percent().gt(80.0), // 낮은 기준으로 시작
            details("헬스체크").failedRequests().count().is(0L)
        );
    }
}