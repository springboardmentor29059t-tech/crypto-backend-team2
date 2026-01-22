package com.crypto.portfolio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private boolean emailNotificationsEnabled = true;
    
    private boolean smsNotificationsEnabled = false;
    
    private String currencyPreference = "USD";
    
    private String theme = "light";
    
    // Constructors
    public UserSettings() {}
    
    public UserSettings(User user) {
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }
    
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }
    
    public boolean isSmsNotificationsEnabled() {
        return smsNotificationsEnabled;
    }
    
    public void setSmsNotificationsEnabled(boolean smsNotificationsEnabled) {
        this.smsNotificationsEnabled = smsNotificationsEnabled;
    }
    
    public String getCurrencyPreference() {
        return currencyPreference;
    }
    
    public void setCurrencyPreference(String currencyPreference) {
        this.currencyPreference = currencyPreference;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
}