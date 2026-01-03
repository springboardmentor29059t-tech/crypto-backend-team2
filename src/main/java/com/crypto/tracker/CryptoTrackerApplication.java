package com.crypto.tracker;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class CryptoTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoTrackerApplication.class, args);
    }
    
    @PostConstruct
    public void init() {
        
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.out.println("Application Timezone set to: " + TimeZone.getDefault().getID());
    }
}