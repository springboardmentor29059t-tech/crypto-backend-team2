package com.crypto.portfolio.tracker.repository;

import com.crypto.portfolio.tracker.entity.ProfitLoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfitLossRepository extends JpaRepository<ProfitLoss, Long> {
    List<ProfitLoss> findByUsername(String username);
    ProfitLoss findBySymbol(String symbol);
}
