package com.crypto.portfolio.tracker.scheduler;

import com.crypto.portfolio.tracker.service.CoinGeckoService;
import com.crypto.portfolio.tracker.service.PriceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {

    private final CoinGeckoService coinGeckoService;
    private final PriceService priceService;

    public PriceScheduler(CoinGeckoService coinGeckoService,
                          PriceService priceService) {
        this.coinGeckoService = coinGeckoService;
        this.priceService = priceService;
    }

    // runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void saveBitcoinPrice() {

        double price = coinGeckoService.getPrice(); // ✅ FIX

        priceService.savePrice("Bitcoin", "BTC", price); // ✅ FIX

        System.out.println("Bitcoin price saved: " + price);
    }
}