package com.crypto.portfolio.tracker.repository;

import com.crypto.portfolio.tracker.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    PriceSnapshot findTopBySymbolOrderByTimestampDesc(String symbol);
    List<PriceSnapshot> findBySymbolOrderByTimestampAsc(String symbol);
}