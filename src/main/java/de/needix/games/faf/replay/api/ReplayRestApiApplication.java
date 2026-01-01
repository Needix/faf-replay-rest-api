package de.needix.games.faf.replay.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = "de.needix.games.faf")
public class ReplayRestApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReplayRestApiApplication.class, args);
    }
}
