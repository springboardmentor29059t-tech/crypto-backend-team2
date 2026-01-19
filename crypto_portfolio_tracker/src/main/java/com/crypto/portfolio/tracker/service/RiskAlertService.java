package com.crypto.portfolio.tracker.service;

import com.crypto.portfolio.tracker.entity.RiskAlert;
import java.util.List;

public interface RiskAlertService {
    RiskAlert addAlert(RiskAlert alert);
    List<RiskAlert> getAllAlerts();
    List<RiskAlert> getAlertsBySymbol(String symbol);
}