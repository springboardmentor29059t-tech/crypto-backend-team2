package com.crypto.tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crypto.tracker.model.Exchange;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    Optional<Exchange> findByName(String name);
    
}