package com.internship.crypto_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scam_tokens")
public class ScamToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "contract_address", nullable = false, unique = true)
    private String contractAddress;

    @Column(nullable = false)
    private String chain; 

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel; 

    private String source; 

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}