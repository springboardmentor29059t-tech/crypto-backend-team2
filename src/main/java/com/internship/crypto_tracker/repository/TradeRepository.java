package com.internship.crypto_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.internship.crypto_tracker.model.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);
    List<Trade> findByUserIdAndAssetSymbolOrderByExecutedAtAsc(Long userId, String assetSymbol);
    
    boolean existsByUserIdAndAssetSymbolAndExecutedAtAndQuantity(
        Long userId, String assetSymbol, java.time.LocalDateTime executedAt, java.math.BigDecimal quantity
    );
}