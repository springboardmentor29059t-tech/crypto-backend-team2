package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.ScamToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScamTokenRepository extends JpaRepository<ScamToken, Long> {
    
    Optional<ScamToken> findByContractAddress(String contractAddress);
}