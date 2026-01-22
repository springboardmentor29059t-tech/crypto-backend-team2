package com.crypto.portfolio.repository;

import com.crypto.portfolio.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByUserId(Long userId);

    Optional<Asset> findByUserIdAndSymbol(Long userId, String symbol);

    @Query("SELECT SUM(a.amount * a.avgBuyPrice) FROM Asset a WHERE a.user.id = :userId")
    BigDecimal calculateTotalPortfolioValue(Long userId);

    Page<Asset> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}