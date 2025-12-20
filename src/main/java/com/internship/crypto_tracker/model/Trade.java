package com.internship.crypto_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side; 

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal price;

    @Column(precision = 20, scale = 10)
    private BigDecimal fee;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    public enum Side {
        BUY, SELL
    }
}