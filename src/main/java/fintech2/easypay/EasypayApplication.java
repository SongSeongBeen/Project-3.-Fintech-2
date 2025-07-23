package fintech2.easypay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 간편 송금 시스템 메인 애플리케이션 클래스
 * Spring Boot 애플리케이션의 진입점 역할
 * JPA Auditing 기능을 활성화하여 엔티티의 생성/수정 시간을 자동으로 관리
 */
@SpringBootApplication
@EnableJpaAuditing
public class EasypayApplication {
    /**
     * 애플리케이션 시작점
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(EasypayApplication.class, args);
    }
}
