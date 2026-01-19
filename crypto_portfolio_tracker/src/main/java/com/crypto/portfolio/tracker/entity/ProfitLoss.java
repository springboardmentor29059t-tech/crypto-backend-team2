package com.crypto.portfolio.tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profit_loss")
public class ProfitLoss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "realized_gain")   // DB column name
    private double realizedPL;

    @Column(name = "unrealized_gain") // DB column name
    private double unrealizedPL;

    @Column(name = "username")
    private String username;

    // Constructors
    public ProfitLoss() {}

    public ProfitLoss(String symbol, double realizedPL, double unrealizedPL, String username) {
        this.symbol = symbol;
        this.realizedPL = realizedPL;
        this.unrealizedPL = unrealizedPL;
        this.username = username;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getRealizedPL() { return realizedPL; }
    public void setRealizedPL(double realizedPL) { this.realizedPL = realizedPL; }

    public double getUnrealizedPL() { return unrealizedPL; }
    public void setUnrealizedPL(double unrealizedPL) { this.unrealizedPL = unrealizedPL; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}