package fintech2.easypay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EasypayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasypayApplication.class, args);
    }
}
