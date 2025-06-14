package myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = { "service", "controller", "models", "console" })
@EntityScan(basePackages = "models")
@EnableJpaRepositories(basePackages = "service.repository")
@EnableKafka
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}