package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 로그인 전용 성능 테스트
 * 계좌 API 오류로 인해 로그인만 테스트
 */
public class LoginOnlyTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Login Test");

    // 사용자 피더
    private FeederBuilder<Object> userFeeder = listFeeder(List.of(
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

    // 로그인 시나리오
    private ScenarioBuilder loginScenario = scenario("로그인 성능 테스트")
        .feed(userFeeder)
        .exec(
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
                .check(jsonPath("$.accountNumber").saveAs("accountNumber"))
        )
        .pause(1, 3);

    // 연속 로그인 시나리오 (세션 유지 테스트)
    private ScenarioBuilder continuousLoginScenario = scenario("연속 로그인 테스트")
        .repeat(5).on(
            feed(userFeeder)
            .exec(
                http("연속 로그인")
                    .post("/auth/login")
                    .body(StringBody("""
                        {
                            "phoneNumber": "#{phoneNumber}",
                            "password": "#{password}"
                        }
                        """)).asJson()
                    .check(status().is(200))
            )
            .pause(2, 5)
        );

    // 헬스체크 시나리오
    private ScenarioBuilder healthCheckScenario = scenario("헬스체크")
        .exec(
            http("시스템 헬스체크")
                .get("/actuator/health")
                .check(status().is(200))
                .check(bodyString().is("{\"status\":\"UP\"}"))
        )
        .pause(15);

    {
        setUp(
            // 기본 로그인 테스트
            loginScenario.injectOpen(
                rampUsers(50).during(Duration.ofMinutes(1)), // 1분간 50명 증가
                constantUsersPerSec(30).during(Duration.ofMinutes(2)), // 2분간 초당 30명
                rampUsers(100).during(Duration.ofMinutes(1)) // 1분간 100명 추가
            ).protocols(httpProtocol),

            // 연속 로그인 테스트
            continuousLoginScenario.injectOpen(
                nothingFor(Duration.ofMinutes(1)),
                constantUsersPerSec(10).during(Duration.ofMinutes(3))
            ).protocols(httpProtocol),

            // 헬스체크
            healthCheckScenario.injectOpen(
                constantUsersPerSec(2).during(Duration.ofMinutes(4))
            ).protocols(httpProtocol)
        )
        .assertions(
            // 전체 성능 목표
            global().responseTime().max().lt(3000), // 최대 3초
            global().responseTime().mean().lt(1000), // 평균 1초
            global().successfulRequests().percent().gt(98.0), // 성공률 98%
            
            // 로그인 특화 목표
            details("사용자 로그인").responseTime().max().lt(2000),
            details("사용자 로그인").successfulRequests().percent().gt(99.0),
            
            // 헬스체크는 항상 성공
            details("시스템 헬스체크").failedRequests().count().is(0L)
        );
    }
}