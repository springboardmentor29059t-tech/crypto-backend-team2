package com.crypto.portfolio.tracker.service;

import com.crypto.portfolio.tracker.entity.Holding;
import com.crypto.portfolio.tracker.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HoldingService {

    private final HoldingRepository repository;

    public HoldingService(HoldingRepository repository) {
        this.repository = repository;
    }

    // Save or update holding
    public Holding saveHolding(Holding holding) {
        return repository.save(holding);
    }

    // Get all holdings of a specific user
    public List<Holding> getHoldingsByUsername(String username) {
        return repository.findByUsername(username);
    }

    // Get single holding by ID
    public Holding getHoldingById(Long id) {
        Optional<Holding> holding = repository.findById(id);
        if (holding.isPresent()) {
            return holding.get();
        } else {
            throw new RuntimeException("Holding not found with ID: " + id);
        }
    }

    // Delete a holding
    public void deleteHolding(Holding holding) {
        repository.delete(holding);
    }
}
