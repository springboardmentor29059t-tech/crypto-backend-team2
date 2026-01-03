package com.crypto.tracker.repository;

import com.crypto.tracker.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    
    Optional<Holding> findByUserIdAndAssetSymbol(Long userId, String assetSymbol);

    List<Holding> findByUserId(Long userId);
}