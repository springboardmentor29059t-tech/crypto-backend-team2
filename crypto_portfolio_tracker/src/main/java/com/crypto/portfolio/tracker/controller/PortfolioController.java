package com.crypto.portfolio.tracker.controller;

import com.crypto.portfolio.tracker.entity.Holding;
import com.crypto.portfolio.tracker.service.HoldingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final HoldingService holdingService;

    public PortfolioController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    // =======================
    // GET: Get all holdings by username
    // Example: GET http://localhost:8082/portfolio?username=riya
    // =======================
    @GetMapping
    public List<Holding> getHoldings(@RequestParam String username) {
        return holdingService.getHoldingsByUsername(username);
    }

    // =======================
    // POST: Add new holding
    // Example: POST http://localhost:8082/portfolio/add
    // Body: JSON { "coinName": "...", "symbol": "...", "quantity": 1, "buyPrice": 1000, "username": "riya" }
    // =======================
    @PostMapping("/add")
    public Holding addHolding(@RequestBody Holding holding) {
        return holdingService.saveHolding(holding);
    }

    // =======================
    // PUT: Update existing holding by ID
    // Example: PUT http://localhost:8082/portfolio/edit/1
    // Body: JSON { "coinName": "...", "symbol": "...", "quantity": 2, "buyPrice": 2000, "username": "riya" }
    // =======================
    @PutMapping("/edit/{id}")
    public Holding updateHolding(@PathVariable Long id, @RequestBody Holding holding) {
        // fetch existing holding first
        Holding existing = holdingService.getHoldingById(id);

        // update fields
        existing.setCoinName(holding.getCoinName());
        existing.setSymbol(holding.getSymbol());
        existing.setQuantity(holding.getQuantity());
        existing.setBuyPrice(holding.getBuyPrice());
        existing.setUsername(holding.getUsername());

        return holdingService.saveHolding(existing);
    }

    // =======================
    // DELETE: Delete holding by ID
    // Example: DELETE http://localhost:8082/portfolio/delete/1
    // =======================
    @DeleteMapping("/delete/{id}")
    public String deleteHolding(@PathVariable Long id) {
        Holding toDelete = holdingService.getHoldingById(id);
        holdingService.deleteHolding(toDelete);
        return "Deleted successfully";
    }
}