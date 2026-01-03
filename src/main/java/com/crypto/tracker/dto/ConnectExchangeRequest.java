package com.crypto.tracker.dto;

public class ConnectExchangeRequest {
    private Long userId;
    private String exchange; 
    private String apiKey;
    private String apiSecret;
    private String label;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}