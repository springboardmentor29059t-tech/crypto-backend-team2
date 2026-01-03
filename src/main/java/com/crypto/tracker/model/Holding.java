package com.crypto.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exchange_id")
    private Long exchangeId;

    @Column(name = "asset_symbol", nullable = false, length = 20)
    private String assetSymbol;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "avg_cost")
    private Double avgCost;

    @Column(name = "wallet_type", nullable = false)
    private String walletType; 

    @Column(name = "address")
    private String address;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public Holding() {}

    public Holding(Long userId, String assetSymbol, Double quantity, Double avgCost, String walletType) {
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.avgCost = avgCost;
        this.walletType = walletType;
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getExchangeId() { return exchangeId; }
    public void setExchangeId(Long exchangeId) { this.exchangeId = exchangeId; }

    // Getters/Setters
    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    // Helper for legacy code 
    public String getSymbol() { return assetSymbol; }
    public void setSymbol(String symbol) { this.assetSymbol = symbol; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getAvgCost() { return avgCost; }
    public void setAvgCost(Double avgCost) { this.avgCost = avgCost; }

    public String getWalletType() { return walletType; }
    public void setWalletType(String walletType) { this.walletType = walletType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}