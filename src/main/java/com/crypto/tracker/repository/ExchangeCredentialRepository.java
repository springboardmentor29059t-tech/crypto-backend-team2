package com.crypto.tracker.repository;

import com.crypto.tracker.model.ExchangeCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExchangeCredentialRepository extends JpaRepository<ExchangeCredential, Long> {
    Optional<ExchangeCredential> findByUserIdAndExchangeName(Long userId, String exchangeName);
    List<ExchangeCredential> findByUserId(Long userId);
}