package com.crypto.portfolio.repository;

import com.crypto.portfolio.model.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    List<PriceSnapshot> findByCryptoSymbol(String cryptoSymbol);
    
    @Query("SELECT ps FROM PriceSnapshot ps WHERE ps.cryptoSymbol = :cryptoSymbol AND ps.snapshotTime >= :fromTime ORDER BY ps.snapshotTime ASC")
    List<PriceSnapshot> findByCryptoSymbolAndSnapshotTimeAfter(String cryptoSymbol, LocalDateTime fromTime);
}