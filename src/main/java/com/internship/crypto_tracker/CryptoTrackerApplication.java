package com.internship.crypto_tracker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class CryptoTrackerApplication {

    public static void main(String[] args) {
        loadEnvVariables(); 

        SpringApplication.run(CryptoTrackerApplication.class, args);
    }

   
    @SuppressWarnings("UseSpecificCatch")
    private static void loadEnvVariables() {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Files.lines(envPath).forEach(line -> {
                    
                    if (line.contains("=") && !line.trim().startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        System.setProperty(key, value);
                    }
                });
                System.out.println("Custom Loader: Successfully loaded .env variables");
            } else {
                System.out.println("Custom Loader: No .env file found (Check file location)");
            }
        } catch (Exception e) {
            System.err.println("Custom Loader: Failed to read .env file");
            e.printStackTrace();
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}