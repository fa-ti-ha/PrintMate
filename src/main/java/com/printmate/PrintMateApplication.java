package com.printmate;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class PrintMateApplication {

    @Value("${printmate.timezone:Asia/Dhaka}")
    private String configuredTimezone;

    public static void main(String[] args) {
        SpringApplication.run(PrintMateApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(configuredTimezone));
    }
}

