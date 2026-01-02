package com.internship.crypto_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "price_snapshots")
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol; 
    @Column(name = "price_usd", nullable = false, precision = 20, scale = 10)
    private BigDecimal priceUsd;

    @Column(name = "market_cap", precision = 30, scale = 2)
    private BigDecimal marketCap;

    @Column(nullable = false)
    private String source; 

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;
}