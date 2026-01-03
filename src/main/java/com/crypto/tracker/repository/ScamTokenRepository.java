package com.crypto.tracker.repository;

import com.crypto.tracker.model.ScamToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScamTokenRepository extends JpaRepository<ScamToken, Long> {
    ScamToken findByContractAddress(String contractAddress);
}