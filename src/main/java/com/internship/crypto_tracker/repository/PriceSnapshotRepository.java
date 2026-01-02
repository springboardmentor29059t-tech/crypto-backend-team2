package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    
    
    List<PriceSnapshot> findByAssetSymbolOrderByCapturedAtAsc(String assetSymbol);

    PriceSnapshot findTopByAssetSymbolOrderByCapturedAtDesc(String assetSymbol);
}