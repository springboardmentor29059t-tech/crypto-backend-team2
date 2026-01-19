package com.crypto.portfolio.tracker.service.impl;

import com.crypto.portfolio.tracker.entity.RiskAlert;
import com.crypto.portfolio.tracker.repository.RiskAlertRepository;
import com.crypto.portfolio.tracker.service.RiskAlertService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RiskAlertServiceImpl implements RiskAlertService {

    private final RiskAlertRepository repository;

    public RiskAlertServiceImpl(RiskAlertRepository repository) {
        this.repository = repository;
    }

    @Override
    public RiskAlert addAlert(RiskAlert alert) {
        return repository.save(alert);
    }

    @Override
    public List<RiskAlert> getAllAlerts() {
        return repository.findAll();
    }

    @Override
    public List<RiskAlert> getAlertsBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }
}