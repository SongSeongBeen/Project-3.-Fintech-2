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
 * 실제 서비스 환경과 유사한 현실적인 부하 테스트 시나리오
 * 
 * 현실적인 사용 패턴:
 * 1. 출퇴근 시간대 트래픽 증가
 * 2. 점심시간 결제 집중
 * 3. 급여일 송금 집중
 * 4. 주말 온라인 쇼핑 증가
 * 5. 오류 발생 및 재시도 패턴
 */
public class RealWorldScenarioSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8090")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("EasyPay Mobile App 1.0")
        .connectionHeader("keep-alive")
        .acceptEncodingHeader("gzip, deflate")
        .silentResources();

    // 실제 사용자 패턴을 반영한 피더
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

    // 점심시간 결제 패턴 (음식점, 카페)
    private FeederBuilder<Object> lunchPaymentFeeder = listFeeder(List.of(
        Map.of("amount", "8000", "merchantName", "김밥천국", "category", "FOOD"),
        Map.of("amount", "12000", "merchantName", "맘스터치", "category", "FOOD"),
        Map.of("amount", "15000", "merchantName", "서브웨이", "category", "FOOD"),
        Map.of("amount", "6000", "merchantName", "스타벅스", "category", "CAFE"),
        Map.of("amount", "4500", "merchantName", "이디야커피", "category", "CAFE"),
        Map.of("amount", "20000", "merchantName", "한식뷔페", "category", "FOOD"),
        Map.of("amount", "13000", "merchantName", "버거킹", "category", "FOOD"),
        Map.of("amount", "9000", "merchantName", "KFC", "category", "FOOD")
    )).random();

    // 온라인 쇼핑 결제 패턴
    private FeederBuilder<Object> onlineShoppingFeeder = listFeeder(List.of(
        Map.of("amount", "35000", "merchantName", "쿠팡", "category", "SHOPPING"),
        Map.of("amount", "89000", "merchantName", "11번가", "category", "SHOPPING"),
        Map.of("amount", "156000", "merchantName", "G마켓", "category", "SHOPPING"),
        Map.of("amount", "45000", "merchantName", "옥션", "category", "SHOPPING"),
        Map.of("amount", "67000", "merchantName", "티몬", "category", "SHOPPING"),
        Map.of("amount", "123000", "merchantName", "위메프", "category", "SHOPPING"),
        Map.of("amount", "234000", "merchantName", "무신사", "category", "FASHION"),
        Map.of("amount", "78000", "merchantName", "29CM", "category", "FASHION")
    )).random();

    // 급여일 송금 패턴
    private FeederBuilder<Object> salaryTransferFeeder = listFeeder(List.of(
        Map.of("amount", "500000", "memo", "생활비"),
        Map.of("amount", "200000", "memo", "용돈"),
        Map.of("amount", "1000000", "memo", "적금"),
        Map.of("amount", "300000", "memo", "부모님 용돈"),
        Map.of("amount", "800000", "memo", "월세"),
        Map.of("amount", "150000", "memo", "대출상환"),
        Map.of("amount", "400000", "memo", "투자금"),
        Map.of("amount", "100000", "memo", "경조사비")
    )).random();

    // 로그인 체인 - 현실적인 재시도 로직 포함
    private ChainBuilder loginChain = exec(
        feed(loginFeeder)
        .doIf(session -> !session.contains("accessToken")).then(
            tryMax(3).on( // 최대 3번 재시도
                exec(
                    http("로그인 시도")
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
                .pause(1, 3) // 재시도 시 대기
            ).exitHereIfFailed()
        )
    );

    // 점심시간 결제 시나리오
    private ChainBuilder lunchPaymentScenario = exec(
        feed(lunchPaymentFeeder)
        .exec(
            http("점심 결제")
                .post("/payments")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    String merchantId = "LUNCH_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                    return String.format("""
                        {
                            "merchantId": "%s",
                            "merchantName": "%s",
                            "amount": %s,
                            "memo": "점심식사",
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
        .pause(Duration.ofSeconds(2), Duration.ofSeconds(5))
        .doIf(session -> session.contains("paymentId")).then(
            exec(
                http("결제 상태 확인")
                    .get("/payments/#{paymentId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
            )
        )
    );

    // 온라인 쇼핑 시나리오 - 다단계 프로세스 시뮬레이션
    private ChainBuilder onlineShoppingScenario = exec(
        // 잔액 확인 (쇼핑 전 습관)
        http("쇼핑전 잔액 확인")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
            .check(jsonPath("$.balance").saveAs("currentBalance"))
    )
    .pause(3, 8) // 상품 둘러보는 시간
    .feed(onlineShoppingFeeder)
    .exec(
        http("온라인 쇼핑 결제")
            .post("/payments")
            .header("Authorization", "Bearer #{accessToken}")
            .body(StringBody(session -> {
                String merchantId = "SHOP_" + ThreadLocalRandom.current().nextInt(10000, 99999);
                return String.format("""
                    {
                        "merchantId": "%s",
                        "merchantName": "%s",
                        "amount": %s,
                        "memo": "온라인 쇼핑",
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
    .pause(1, 2)
    // 결제 후 바로 내역 확인하는 패턴
    .exec(
        http("쇼핑후 잔액 확인")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    );

    // 급여일 송금 시나리오
    private ChainBuilder salaryDayTransferScenario = exec(
        // 급여 들어왔는지 잔액 확인
        http("급여일 잔액 확인")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
            .check(jsonPath("$.balance").saveAs("currentBalance"))
    )
    .pause(2, 5)
    .repeat(2, "transferCount").on( // 급여일에는 보통 2-3번 송금
        feed(salaryTransferFeeder)
        .exec(
            http("급여일 송금")
                .post("/transfers")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(session -> {
                    // 실제 계좌 번호 사용 (네트워크 내)
                    // 실제 수신자 계좌 사용
                    String[] receiverAccounts = {"VA52868263189", "VA52796358138", "VA26968382193", "VA26356435241", "VA7532721438"};
                    String receiverAccount = receiverAccounts[ThreadLocalRandom.current().nextInt(receiverAccounts.length)];
                    return String.format("""
                        {
                            "receiverAccountNumber": "%s",
                            "amount": %s,
                            "memo": "%s"
                        }
                        """, 
                        receiverAccount,
                        session.getString("amount"),
                        session.getString("memo")
                    );
                })).asJson()
                .check(status().in(200, 201))
                .check(jsonPath("$.transferId").optional().saveAs("transferId"))
        )
        .pause(3, 7)
        .doIf(session -> session.contains("transferId")).then(
            exec(
                http("송금 상태 확인")
                    .get("/transfers/#{transferId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
            )
        )
        .pause(5, 15) // 다음 송금까지 대기
    );

    // 일반적인 앱 사용 패턴 (조회 위주)
    private ChainBuilder casualBrowsingScenario = exec(
        http("캐주얼 잔액 조회")
            .get("/accounts/balance")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    )
    .pause(5, 12)
    .exec(
        http("최근 거래내역 확인")
            .get("/transfers/history")
            .header("Authorization", "Bearer #{accessToken}")
            .queryParam("page", "0")
            .queryParam("size", "10")
            .check(status().is(200))
    )
    .pause(8, 20)
    // 30% 확률로 소액 결제
    .randomSwitch().on(
        Choice.withWeight(30.0,
            exec(
                http("즉석 소액결제")
                    .post("/payments")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody(session -> {
                        int amount = ThreadLocalRandom.current().nextInt(3000, 15000);
                        String merchantId = "SMALL_" + ThreadLocalRandom.current().nextInt(1000, 9999);
                        return String.format("""
                            {
                                "merchantId": "%s",
                                "merchantName": "편의점",
                                "amount": %d,
                                "memo": "소액결제",
                                "paymentMethod": "BALANCE"
                            }
                            """, merchantId, amount);
                    })).asJson()
                    .check(status().in(200, 201))
            )
        ),
        Choice.withWeight(70.0, exec(pause(3, 8))) // 70%는 그냥 둘러보기만
    );

    // 시나리오 정의
    private ScenarioBuilder lunchRushScenario = scenario("점심시간 러시")
        .exec(loginChain)
        .exec(lunchPaymentScenario);

    private ScenarioBuilder weekendShoppingScenario = scenario("주말 온라인쇼핑")
        .exec(loginChain)
        .exec(onlineShoppingScenario);

    private ScenarioBuilder salaryDayScenario = scenario("급여일 송금")
        .exec(loginChain)
        .exec(salaryDayTransferScenario);

    private ScenarioBuilder casualUserScenario = scenario("일반 사용자")
        .exec(loginChain)
        .exec(casualBrowsingScenario);

    // 시스템 모니터링 시나리오
    private ScenarioBuilder systemMonitoringScenario = scenario("시스템 모니터링")
        .exec(
            http("헬스체크")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(30); // 30초마다 체크

    {
        setUp(
            // 시나리오 1: 점심시간 러시 (12:00-13:00 시뮬레이션)
            lunchRushScenario.injectOpen(
                nothingFor(Duration.ofSeconds(30)),
                rampUsers(80).during(Duration.ofMinutes(2)), // 급격한 증가
                constantUsersPerSec(40).during(Duration.ofMinutes(3)), // 피크 유지
                rampUsers(20).during(Duration.ofMinutes(1)) // 점진적 감소
            ).protocols(httpProtocol),

            // 시나리오 2: 주말 온라인쇼핑 (여유로운 패턴)
            weekendShoppingScenario.injectOpen(
                nothingFor(Duration.ofMinutes(1)),
                rampUsers(30).during(Duration.ofMinutes(3)), // 서서히 증가
                constantUsersPerSec(8).during(Duration.ofMinutes(5)), // 안정적 트래픽
                rampUsers(50).during(Duration.ofMinutes(2)) // 저녁 시간대 증가
            ).protocols(httpProtocol),

            // 시나리오 3: 급여일 (월 1-2회)
            salaryDayScenario.injectOpen(
                nothingFor(Duration.ofMinutes(2)),
                rampUsers(60).during(Duration.ofMinutes(1)), // 급여 확인 후 송금 러시
                constantUsersPerSec(20).during(Duration.ofMinutes(4)), // 지속적인 송금
                rampUsers(40).during(Duration.ofMinutes(1)) // 마지막 송금들
            ).protocols(httpProtocol),

            // 시나리오 4: 일반 사용자 (백그라운드 트래픽)
            casualUserScenario.injectOpen(
                constantUsersPerSec(3).during(Duration.ofMinutes(12)) // 꾸준한 백그라운드 트래픽
            ).protocols(httpProtocol),

            // 시나리오 5: 시스템 모니터링
            systemMonitoringScenario.injectOpen(
                constantUsersPerSec(1).during(Duration.ofMinutes(12))
            ).protocols(httpProtocol)
        )
        .assertions(
            // 전체 성능 목표
            global().responseTime().max().lt(12000), // 최대 12초 (현실적 목표)
            global().responseTime().mean().lt(3000), // 평균 3초
            global().responseTime().percentile3().lt(6000), // 95% 6초 이하
            global().successfulRequests().percent().gt(85.0), // 성공률 85% (현실적)

            // 점심시간 러시 특별 기준
            details("점심 결제").responseTime().max().lt(8000),
            details("점심 결제").successfulRequests().percent().gt(90.0),

            // 온라인쇼핑 기준 (상대적으로 여유)
            details("온라인 쇼핑 결제").responseTime().mean().lt(4000),

            // 급여일 송금 기준
            details("급여일 송금").responseTime().max().lt(15000), // 송금은 더 오래 걸릴 수 있음
            details("급여일 송금").successfulRequests().percent().gt(95.0), // 송금은 실패하면 안됨

            // 조회 API는 빨라야 함
            details("잔액 확인").responseTime().max().lt(3000),
            details("거래내역 확인").responseTime().max().lt(4000),

            // 헬스체크는 항상 성공
            details("헬스체크").failedRequests().count().is(0L)
        );
    }
}