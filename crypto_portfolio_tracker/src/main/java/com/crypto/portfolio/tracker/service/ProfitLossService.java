package com.crypto.portfolio.tracker.service;

import com.crypto.portfolio.tracker.entity.ProfitLoss;

import java.util.List;

public interface ProfitLossService {
    ProfitLoss addProfitLoss(ProfitLoss pl);
    List<ProfitLoss> getAllProfitLoss();
    List<ProfitLoss> getByUsername(String username);
    ProfitLoss calculateProfitLoss(String symbol);
}