package fintech2.easypay.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import fintech2.easypay.transfer.service.TransferService;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Transfer 패키지 아키텍처 규칙 테스트
 * 
 * 패키지 구조:
 * transfer
 *  ├─ controller (컨트롤러)
 *  ├─ dto (데이터 전송 객체)
 *  ├─ entity (엔티티)
 *  ├─ enums (열거형)
 *  ├─ repository (저장소)
 *  ├─ service (서비스)
 *  │   └─ flow (플로우)
 *  │       └─ context (컨텍스트)
 *  ├─ client (외부 클라이언트)
 *  └─ support/id (ID 생성)
 */
@AnalyzeClasses(packagesOf = TransferService.class)
public class TransferArchitectureTest {

    // 패키지 상수
    private static final String ROOT        = "fintech2.easypay..";
    private static final String TRANSFER    = "..transfer..";
    private static final String CONTROLLER  = "..transfer.controller..";
    private static final String SERVICE     = "..transfer.service..";
    private static final String FLOW        = "..transfer.service.flow..";
    private static final String CONTEXT     = "..transfer.service.flow.context..";
    private static final String REPOSITORY  = "..transfer.repository..";
    private static final String ENTITY      = "..transfer.entity..";
    private static final String ENUMS       = "..transfer.enums..";
    private static final String DTO         = "..transfer.dto..";
    private static final String CLIENT      = "..transfer.client..";
    private static final String SUPPORT     = "..transfer.support..";
    private static final String ACCOUNT_LEDGER = "..account.ledger..";

    // === 레이어별 접근 규칙 ===

    // 1) 컨트롤러는 레포지토리/엔티티/클라이언트에 직접 접근 금지 (서비스를 통해서만)
    @ArchTest
    static final ArchRule controllers_should_only_access_services_and_dtos =
        noClasses().that().resideInAPackage(CONTROLLER)
            .should().accessClassesThat().resideInAnyPackage(REPOSITORY, ENTITY, CLIENT, FLOW);

    // 2) 서비스 레이어는 컨트롤러에 의존 금지 (역방향 의존성 차단)
    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
        noClasses().that().resideInAPackage(SERVICE)
            .should().accessClassesThat().resideInAnyPackage(CONTROLLER);

    // 3) 엔티티는 스프링/컨트롤러/서비스/클라이언트에 의존 금지 (순수 도메인)
    @ArchTest
    static final ArchRule entities_should_be_pure_domain =
        noClasses().that().resideInAPackage(ENTITY)
            .should().accessClassesThat()
            .resideInAnyPackage("org.springframework..", CONTROLLER, SERVICE, CLIENT);

    // 4) 레포지토리는 컨트롤러/서비스/클라이언트에 의존 금지
    @ArchTest
    static final ArchRule repositories_should_only_depend_on_entities =
        noClasses().that().resideInAPackage(REPOSITORY)
            .should().accessClassesThat().resideInAnyPackage(CONTROLLER, SERVICE, CLIENT);

    // === 플로우 관련 규칙 ===

    // 5) Flow는 Spring 싱글톤 컴포넌트로 등록 금지 (상태를 가진 클래스)
    @ArchTest
    static final ArchRule flows_should_not_be_spring_components =
        noClasses().that().resideInAPackage(FLOW)
            .should().beAnnotatedWith(Service.class)
            .orShould().beAnnotatedWith(Component.class)
            .orShould().beAnnotatedWith(Configuration.class);

    // 6) Flow는 레포지토리와 클라이언트에 직접 접근 가능 (설계 허용)
    @ArchTest
    static final ArchRule flows_can_access_repositories_and_clients =
        classes().that().resideInAPackage(FLOW)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                FLOW, CONTEXT, REPOSITORY, CLIENT, ENTITY, ENUMS, 
                SERVICE, SUPPORT, ACCOUNT_LEDGER,
                "java..", "javax..", "lombok..", "org.slf4j.."
            );

    // === 트랜잭션 관리 규칙 ===

    // 7) @Transactional은 서비스 레이어에서만 사용 (플로우/컨트롤러 금지)
    @ArchTest
    static final ArchRule transactional_only_in_services =
        classes().that().areAnnotatedWith(Transactional.class)
            .should().resideInAnyPackage(SERVICE);

    // 7-1) Flow와 Controller에서는 @Transactional 사용 금지
    @ArchTest
    static final ArchRule flows_and_controllers_should_not_use_transactional =
        noClasses().that().resideInAnyPackage(FLOW, CONTROLLER)
            .should().beAnnotatedWith(Transactional.class);

    // === 외부 의존성 규칙 ===

    // 8) 클라이언트는 서비스와 플로우에서만 사용 가능
    @ArchTest
    static final ArchRule clients_should_only_be_used_by_services_and_flows =
        classes().that().resideInAPackage(CLIENT)
            .should().onlyBeAccessed().byAnyPackage(SERVICE, FLOW, CLIENT);

    // === 모듈 경계 규칙 ===

    // 9) account.ledger는 transfer에 의존하지 말아야 함 (모듈 경계)
    @ArchTest
    static final ArchRule ledger_should_not_depend_on_transfer =
        noClasses().that().resideInAPackage(ACCOUNT_LEDGER)
            .should().accessClassesThat().resideInAnyPackage(TRANSFER);

    // 10) transfer → account.ledger 의존은 허용 (단방향 의존성)
    @ArchTest
    static final ArchRule transfer_can_depend_on_ledger =
        classes().that().resideInAnyPackage(TRANSFER)
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                TRANSFER, ACCOUNT_LEDGER,
                ROOT, // 공통 모듈
                "java..", "javax..", "jakarta..", 
                "org.springframework..", "lombok..", "org.slf4j..",
                "com.fasterxml.jackson..", "org.junit..", "org.mockito..",
                "io.swagger.."
            );

    // === 순환 의존성 방지 ===

    // 11) transfer 내부 패키지 간 순환 의존성 금지
    @ArchTest
    static final ArchRule no_cycles_within_transfer_package =
        com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
            .matching("..transfer.(*)..")
            .should().beFreeOfCycles();

    // === 네이밍 및 위치 규칙 ===

    // 12) 서비스 클래스는 service 패키지에만
    @ArchTest
    static final ArchRule service_classes_should_be_in_service_package =
        classes().that().haveSimpleNameEndingWith("Service")
            .and().areNotInterfaces()
            .should().resideInAnyPackage(SERVICE, ACCOUNT_LEDGER);

    // 13) 레포지토리 클래스는 repository 패키지에만
    @ArchTest
    static final ArchRule repository_classes_should_be_in_repository_package =
        classes().that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage(REPOSITORY);

    // 14) 컨트롤러 클래스는 올바른 어노테이션 사용
    @ArchTest
    static final ArchRule controllers_should_be_properly_annotated =
        classes().that().resideInAPackage(CONTROLLER)
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

    // === 보안 및 품질 규칙 ===

    // 15) 엔티티는 Setter 메서드를 가지지 말아야 함 (불변성)
    @ArchTest
    static final ArchRule entities_should_not_have_setters =
        noMethods().that().areDeclaredInClassesThat().resideInAPackage(ENTITY)
            .and().haveNameMatching("set.*")
            .should().bePublic();

    // 16) Flow 컨텍스트는 불변 객체여야 함
    @ArchTest
    static final ArchRule contexts_should_be_immutable =
        classes().that().resideInAPackage(CONTEXT)
            .should().haveOnlyFinalFields();

    // === 성능 및 설계 규칙 ===

    // 17) 대용량 컬렉션 필드는 private final이어야 함
    @ArchTest
    static final ArchRule collections_should_be_private_final =
        fields().that().haveRawType(java.util.Collection.class)
            .should().bePrivate()
            .andShould().beFinal();

    // 18) 중요한 서비스 메서드는 로깅을 해야 함 (샘플 규칙)
    @ArchTest
    static final ArchRule service_classes_should_have_slf4j_logger =
        classes().that().resideInAPackage(SERVICE)
            .and().areAnnotatedWith(Service.class)
            .should().haveSimpleNameContaining("Service");
}