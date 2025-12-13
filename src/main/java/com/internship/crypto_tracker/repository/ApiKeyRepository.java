package com.internship.crypto_tracker.repository;

import com.internship.crypto_tracker.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    
    List<ApiKey> findByUserId(Long userId);

    boolean existsByKey(String key);
}