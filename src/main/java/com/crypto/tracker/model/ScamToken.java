package com.crypto.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scam_tokens")
public class ScamToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Column(name = "contract_address")
    private String contractAddress;

    private String chain; // "chain" matches "chain", so no annotation needed

    @Column(name = "risk_level")
    private String riskLevel;

    private String source;

   
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }

    public String getChain() { return chain; }
    public void setChain(String chain) { this.chain = chain; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
}