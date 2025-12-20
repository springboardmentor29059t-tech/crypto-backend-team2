package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    
    List<Holding> findByUserId(Long userId);
}