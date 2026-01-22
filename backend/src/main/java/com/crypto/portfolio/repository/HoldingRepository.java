package com.crypto.portfolio.repository;

import com.crypto.portfolio.model.Holding;
import com.crypto.portfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUser(User user);
    List<Holding> findByUserAndCryptoSymbol(User user, String cryptoSymbol);
}