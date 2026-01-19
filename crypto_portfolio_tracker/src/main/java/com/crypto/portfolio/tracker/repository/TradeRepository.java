package com.crypto.portfolio.tracker.repository;

import com.crypto.portfolio.tracker.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbol(String symbol);
    List<Trade> findByUsername(String username);
}