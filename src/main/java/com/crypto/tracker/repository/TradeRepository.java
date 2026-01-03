package com.crypto.tracker.repository;

import com.crypto.tracker.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);
}