package com.crypto.portfolio.tracker.repository;

import com.crypto.portfolio.tracker.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUsername(String username);
}

