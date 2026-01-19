package com.crypto.portfolio.tracker.controller;

import com.crypto.portfolio.tracker.entity.ProfitLoss;
import com.crypto.portfolio.tracker.service.ProfitLossService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pl")
public class ProfitLossController {

    private final ProfitLossService service;

    public ProfitLossController(ProfitLossService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ProfitLoss addPL(@RequestBody ProfitLoss pl) {
        return service.addProfitLoss(pl);
    }

    @GetMapping("/all")
    public List<ProfitLoss> getAllPL() {
        return service.getAllProfitLoss();
    }

    @GetMapping("/user/{username}")
    public List<ProfitLoss> getPLByUser(@PathVariable String username) {
        return service.getByUsername(username);
    }

    @GetMapping("/{symbol}")
    public ProfitLoss getPL(@PathVariable String symbol) {
        return service.calculateProfitLoss(symbol);
    }
}