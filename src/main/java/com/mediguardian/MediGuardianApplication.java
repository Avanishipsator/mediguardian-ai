package com.mediguardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MediGuardianApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediGuardianApplication.class, args);
    }
}
