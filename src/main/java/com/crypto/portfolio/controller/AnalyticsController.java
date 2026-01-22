package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.AllocationDto;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/allocation")
    public ResponseEntity<List<AllocationDto>> getAllocation(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getAllocation(getUserId(userDetails)));
    }

    @GetMapping("/health-score")
    public ResponseEntity<Map<String, Object>> getHealthScore(@AuthenticationPrincipal UserDetails userDetails) {
        int score = analyticsService.getPortfolioHealthScore(getUserId(userDetails));
        return ResponseEntity.ok(Map.of(
                "score", score,
                "status", score > 70 ? "Excellent" : (score > 40 ? "Good" : "Needs Improvement")));
    }
}
