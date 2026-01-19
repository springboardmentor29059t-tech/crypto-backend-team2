package com.crypto.portfolio.tracker.repository;

import com.crypto.portfolio.tracker.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findBySymbol(String symbol);
}