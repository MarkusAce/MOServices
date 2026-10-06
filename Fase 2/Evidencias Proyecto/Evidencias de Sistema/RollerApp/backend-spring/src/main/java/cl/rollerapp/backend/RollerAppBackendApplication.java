package cl.rollerapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RollerAppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RollerAppBackendApplication.class, args);
    }
}
