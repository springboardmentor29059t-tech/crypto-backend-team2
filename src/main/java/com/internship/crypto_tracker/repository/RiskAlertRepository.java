package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findByUserId(Long userId);
}