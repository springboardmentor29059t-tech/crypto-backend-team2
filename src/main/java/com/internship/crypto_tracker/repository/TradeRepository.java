package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);
    
    boolean existsByUserIdAndAssetSymbolAndExecutedAtAndQuantity(
        Long userId, String assetSymbol, java.time.LocalDateTime executedAt, java.math.BigDecimal quantity
    );
}