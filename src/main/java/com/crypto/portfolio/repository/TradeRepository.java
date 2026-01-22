package com.crypto.portfolio.repository;

import com.crypto.portfolio.model.Trade;
import com.crypto.portfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUser(User user);

    List<Trade> findByUserAndAssetSymbol(User user, String assetSymbol);
}
