package com.crypto.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_snapshots")
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(name = "price_usd", nullable = false)
    private Double price;

    @Column(name = "market_cap")
    private Double marketCap;

    @Column(name = "source")
    private String source;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    // --- Constructors ---
    public PriceSnapshot() {}

    public PriceSnapshot(String assetSymbol, Double price, LocalDateTime capturedAt) {
        this.assetSymbol = assetSymbol;
        this.price = price;
        this.capturedAt = capturedAt; 
        this.source = "CoinGecko"; 
    }

    // Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getMarketCap() { return marketCap; }
    public void setMarketCap(Double marketCap) { this.marketCap = marketCap; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
}