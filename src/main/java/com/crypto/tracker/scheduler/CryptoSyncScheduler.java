package com.crypto.tracker.scheduler;

import com.crypto.tracker.model.User;
import com.crypto.tracker.repository.UserRepository;
import com.crypto.tracker.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@EnableScheduling
public class CryptoSyncScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeService exchangeService;

    // Run every 5 minutes (300,000 ms)
    @Scheduled(fixedRate = 300000) 
    public void syncAllPortfolios() {
        System.out.println("--- STARTING SCHEDULED PORTFOLIO SYNC ---");
        
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                
                exchangeService.syncTrades(user.getId());
            } catch (Exception e) {
                System.err.println("Error syncing user " + user.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("--- FINISHED SCHEDULED SYNC ---");
    }
}