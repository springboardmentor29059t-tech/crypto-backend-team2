package com.crypto.portfolio.tracker.controller;

import com.crypto.portfolio.tracker.entity.RiskAlert;
import com.crypto.portfolio.tracker.service.RiskAlertService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAlertController {

    private final RiskAlertService service;

    public RiskAlertController(RiskAlertService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public RiskAlert addAlert(@RequestBody RiskAlert alert) {
        return service.addAlert(alert);
    }

    @GetMapping("/all")
    public List<RiskAlert> getAllAlerts() {
        return service.getAllAlerts();
    }

    @GetMapping("/symbol/{symbol}")
    public List<RiskAlert> getAlertsBySymbol(@PathVariable String symbol) {
        return service.getAlertsBySymbol(symbol);
    }
}
