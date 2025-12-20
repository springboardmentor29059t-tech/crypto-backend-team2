package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByUserIdAndExchangeName(Long userId, String exchangeName);
    List<ApiKey> findByUserId(Long userId);

    boolean existsByApiKey(String apiKey);
}