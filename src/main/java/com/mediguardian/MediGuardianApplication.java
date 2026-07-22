package com.mediguardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MediGuardianApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediGuardianApplication.class, args);
    }
}
