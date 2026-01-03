package com.crypto.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_credentials")
public class ExchangeCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @Column(name = "encrypted_api_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedApiKey;

    @Column(name = "encrypted_secret", nullable = false, columnDefinition = "TEXT")
    private String encryptedSecret;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "label")
    private String label;

    // Constructors
    public ExchangeCredential() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
    
    public String getEncryptedApiKey() { return encryptedApiKey; }
    public void setEncryptedApiKey(String apiKey) { this.encryptedApiKey = apiKey; }
    
    public String getEncryptedSecret() { return encryptedSecret; }
    public void setEncryptedSecret(String secret) { this.encryptedSecret = secret; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}