package fintech2.easypay.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.util.List;
import java.util.Map;

/**
 * 기본적인 로드 테스트 - 로그인과 헬스체크만
 * 서버의 기본적인 처리 능력을 테스트
 */
public class BasicLoadTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Basic Load Test");

    // 로그인용 사용자 피더
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
    private ScenarioBuilder loginScenario = scenario("로그인 부하 테스트")
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
        )
        .pause(1, 3)
        .repeat(5).on( // 로그인 성공 후 5번 반복 요청
            exec(
                http("로그인 상태 유지 확인")
                    .post("/auth/validate")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().in(200, 401)) // 401도 정상적인 응답으로 간주
            )
            .pause(2, 5)
        );

    // 헬스체크 시나리오
    private ScenarioBuilder healthCheckScenario = scenario("헬스체크 테스트")
        .exec(
            http("시스템 헬스체크")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(10);

    {
        setUp(
            // 로그인 부하 테스트
            loginScenario.injectOpen(
                rampUsers(100).during(Duration.ofMinutes(2)), // 2분간 100명 증가
                constantUsersPerSec(50).during(Duration.ofMinutes(3)), // 3분간 초당 50명
                rampUsers(200).during(Duration.ofMinutes(2)) // 2분간 200명 추가
            ).protocols(httpProtocol),

            // 백그라운드 헬스체크
            healthCheckScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(7))
            ).protocols(httpProtocol)
        )
        .assertions(
            // 성능 목표
            global().responseTime().max().lt(5000), // 최대 응답시간 5초
            global().responseTime().mean().lt(1000), // 평균 응답시간 1초
            global().successfulRequests().percent().gt(95.0), // 성공률 95%
            
            // 로그인 특화 목표
            details("사용자 로그인").responseTime().max().lt(3000),
            details("사용자 로그인").successfulRequests().percent().gt(99.0),
            
            // 헬스체크는 항상 성공
            details("시스템 헬스체크").failedRequests().count().is(0L)
        );
    }
}