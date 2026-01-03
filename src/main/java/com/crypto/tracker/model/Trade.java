package com.crypto.tracker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exchange_name")
    @JsonProperty("exchange") // Sends "exchange": "Binance" to frontend
    private String exchangeName;

    @Column(name = "asset_symbol", nullable = false)
    @JsonProperty("symbol") // Sends "symbol": "BTC" to frontend
    private String assetSymbol;

    @Column(name = "side", nullable = false)
    private String side; // "BUY" or "SELL"

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "fee")
    private Double fee;

    @Column(name = "executed_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    @Column(name = "storage_type")
    private String storageType; 

    // --- Constructors ---
    public Trade() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getExchangeName() { return exchangeName; }
    public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }

    // --- toString for Logging ---
    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", symbol='" + assetSymbol + '\'' +
                ", side='" + side + '\'' +
                ", qty=" + quantity +
                ", price=" + price +
                '}';
    }
}