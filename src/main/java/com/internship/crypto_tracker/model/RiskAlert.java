package com.internship.crypto_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "risk_alerts")
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type")
    private AlertType alertType;

    @Column(columnDefinition = "TEXT")
    private String details; 

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum AlertType {
        RUGPULL_WARNING, CONTRACT_RISK, NEWS
    }
}