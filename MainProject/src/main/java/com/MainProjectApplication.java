package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class MainProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainProjectApplication.class, args);
    }

    // Force JVM timezone to UTC at startup
    // This prevents "Asia/Calcutta" being sent to Docker PostgreSQL
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
