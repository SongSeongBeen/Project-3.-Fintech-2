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
 * 송금 서비스 대용량 트래픽 부하 테스트
 * 
 * 테스트 시나리오:
 * 1. 사용자 로그인
 * 2. 송금 요청 (다양한 금액, 수신자)
 * 3. 송금 상태 확인
 * 4. 송금 내역 조회
 */
public class TransferLoadTestSimulation extends Simulation {

    // HTTP 프로토콜 설정
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Transfer Test");

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

    // 실제 생성된 수신자 계좌 피더
    private FeederBuilder<Object> receiverFeeder = listFeeder(List.of(
        Map.of("receiverAccount", "VA52868263189", "receiverName", "테스트유저젇6"),
        Map.of("receiverAccount", "VA52796358138", "receiverName", "테스트유저젇7"),
        Map.of("receiverAccount", "VA26968382193", "receiverName", "테스트유저젇8"),
        Map.of("receiverAccount", "VA26356435241", "receiverName", "테스트유저젇9"),
        Map.of("receiverAccount", "VA7532721438", "receiverName", "테스트유저젇10")
    )).random();

    // 송금 금액과 메모 피더
    private FeederBuilder<Object> transferAmountFeeder = listFeeder(List.of(
        Map.of("amount", "10000", "memo", "커피값"),
        Map.of("amount", "20000", "memo", "점심값"),
        Map.of("amount", "50000", "memo", "생일축하금"),
        Map.of("amount", "100000", "memo", "용돈"),
        Map.of("amount", "200000", "memo", "월세지원"),
        Map.of("amount", "500000", "memo", "경조사비"),
        Map.of("amount", "1000000", "memo", "대출상환"),
        Map.of("amount", "2000000", "memo", "투자금"),
        Map.of("amount", "3000000", "memo", "사업자금"),
        Map.of("amount", "5000000", "memo", "부동산계약금")
    )).random();

    // 향상된 로그인 시나리오 - 토큰 재사용
    private ChainBuilder loginChain = exec(
        feed(loginFeeder)
        .doIf(session -> !session.contains("accessToken") || 
               session.getString("accessToken") == null || 
               session.getString("accessToken").isEmpty()).then(
            exec(
                http("송금자 로그인")
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

    // 송금 시나리오
    private ChainBuilder transferChain = exec(
        feed(receiverFeeder)
        .feed(transferAmountFeeder)
        .exec(
            http("송금 요청")
                .post("/transfers")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody("""
                    {
                        "receiverAccountNumber": "#{accountNumber}",
                        "amount": #{amount},
                        "memo": "#{memo} - Gatling 테스트"
                    }
                    """)).asJson()
                .check(status().in(200, 201))
                .check(jsonPath("$.transferId").optional().saveAs("transferId"))
                .check(jsonPath("$.status").optional().saveAs("transferStatus"))
        )
        .pause(1, 2)
        .doIf(session -> session.contains("transferId")).then(
            exec(
                http("송금 상태 확인")
                    .get("/transfers/#{transferId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
            )
        )
    );

    // 송금 내역 조회 시나리오
    private ChainBuilder transferHistoryChain = exec(
        http("송금 내역 조회")
            .get("/transfers/history")
            .header("Authorization", "Bearer #{accessToken}")
            .queryParam("page", "0")
            .queryParam("size", "10")
            .check(status().is(200))
    );

    // 계좌 잔액 확인 시나리오
    private ChainBuilder balanceCheckChain = exec(
        http("잔액 조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
            .check(jsonPath("$.balance").saveAs("balance"))
    );

    // 일반 송금 시나리오
    private ScenarioBuilder normalTransferScenario = scenario("일반 송금 부하 테스트")
        .exec(loginChain)
        .pause(1, 2)
        .exec(balanceCheckChain)
        .pause(1, 2)
        .repeat(2).on( // 사용자당 2번의 송금
            exec(transferChain)
            .pause(3, 7)
        )
        .exec(transferHistoryChain);

    // 대량 송금 시나리오 (급여이체, 배당금 지급 등)
    private ScenarioBuilder bulkTransferScenario = scenario("대량 송금 테스트")
        .exec(loginChain)
        .pause(1)
        .exec(balanceCheckChain)
        .repeat(5).on( // 한 번에 5건의 송금
            exec(transferChain)
            .pause(Duration.ofMillis(500), Duration.ofSeconds(2))
        );

    // 연속 송금 시나리오 (송금 앱 heavy user)
    private ScenarioBuilder continuousTransferScenario = scenario("연속 송금 테스트")
        .exec(loginChain)
        .pause(2)
        .during(Duration.ofMinutes(3)).on( // 3분간 지속적으로 송금
            exec(transferChain)
            .pace(Duration.ofSeconds(15)) // 15초마다 송금
        );

    // 송금 조회 중심 시나리오 (조회만 많이 하는 사용자)
    private ScenarioBuilder transferInquiryScenario = scenario("송금 조회 테스트")
        .exec(loginChain)
        .pause(1)
        .repeat(10).on( // 10번 조회
            exec(balanceCheckChain)
            .pause(1, 2)
            .exec(transferHistoryChain)
            .pause(2, 4)
        );

    {
        setUp(
            // 시나리오 1: 일반적인 송금 부하 테스트
            normalTransferScenario.injectOpen(
                rampUsers(30).during(Duration.ofMinutes(2)), // 2분간 30명 증가
                constantUsersPerSec(10).during(Duration.ofMinutes(4)), // 4분간 초당 10명
                rampUsers(70).during(Duration.ofMinutes(2)) // 2분간 70명 추가
            ).protocols(httpProtocol),

            // 시나리오 2: 대량 송금 (급여이체 시간대)
            bulkTransferScenario.injectOpen(
                nothingFor(Duration.ofMinutes(1)), // 1분 대기
                rampUsers(50).during(Duration.ofMinutes(1)), // 1분간 50명 증가
                constantUsersPerSec(25).during(Duration.ofMinutes(2)) // 2분간 초당 25명
            ).protocols(httpProtocol),

            // 시나리오 3: 연속 송금 사용자
            continuousTransferScenario.injectOpen(
                constantUsersPerSec(3).during(Duration.ofMinutes(5)) // 5분간 초당 3명
            ).protocols(httpProtocol),

            // 시나리오 4: 조회 중심 트래픽
            transferInquiryScenario.injectOpen(
                rampUsers(100).during(Duration.ofMinutes(1)), // 1분간 100명 증가
                constantUsersPerSec(15).during(Duration.ofMinutes(3)) // 3분간 초당 15명
            ).protocols(httpProtocol)
        )
        .assertions(
            // 성능 기준 설정
            global().responseTime().max().lt(8000), // 최대 응답시간 8초 이하 (송금은 더 복잡한 로직)
            global().responseTime().mean().lt(2000), // 평균 응답시간 2초 이하
            global().responseTime().percentile3().lt(3000), // 95 퍼센타일 3초 이하
            global().successfulRequests().percent().gt(90.0), // 성공률 90% 이상
            
            // 특정 요청별 성능 기준
            details("송금 요청").responseTime().max().lt(10000), // 송금 요청 최대 10초
            details("잔액 조회").responseTime().max().lt(2000), // 잔액 조회 최대 2초
            details("송금 내역 조회").responseTime().max().lt(3000) // 내역 조회 최대 3초
        );
    }
}