package goormton.univ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoormtonUnivApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoormtonUnivApplication.class, args);
    }
}