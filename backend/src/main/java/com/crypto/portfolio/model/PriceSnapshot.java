package com.crypto.portfolio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_snapshots")
public class PriceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String cryptoSymbol;
    
    @Column(nullable = false)
    private String cryptoName;
    
    @Column(nullable = false)
    private double price;
    
    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;
    
    // Constructors
    public PriceSnapshot() {}
    
    public PriceSnapshot(String cryptoSymbol, String cryptoName, double price) {
        this.cryptoSymbol = cryptoSymbol;
        this.cryptoName = cryptoName;
        this.price = price;
        this.snapshotTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCryptoSymbol() {
        return cryptoSymbol;
    }
    
    public void setCryptoSymbol(String cryptoSymbol) {
        this.cryptoSymbol = cryptoSymbol;
    }
    
    public String getCryptoName() {
        return cryptoName;
    }
    
    public void setCryptoName(String cryptoName) {
        this.cryptoName = cryptoName;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }
    
    public void setSnapshotTime(LocalDateTime snapshotTime) {
        this.snapshotTime = snapshotTime;
    }
}