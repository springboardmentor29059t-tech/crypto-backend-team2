package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    Optional<Exchange> findByName(String name);
}
