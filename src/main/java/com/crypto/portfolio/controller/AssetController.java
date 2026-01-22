package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.AssetRequestDTO;
import com.crypto.portfolio.dto.AssetResponseDTO;
import com.crypto.portfolio.service.AssetService;
import com.crypto.portfolio.service.MarketService;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "http://localhost:3000") // Adjust port if frontend runs elsewhere (user said 3000)
public class AssetController {

    private final AssetService assetService;
    private final UserRepository userRepository;
    private final MarketService marketService;

    public AssetController(AssetService assetService, UserRepository userRepository, MarketService marketService) {
        this.assetService = assetService;
        this.userRepository = userRepository;
        this.marketService = marketService;
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<AssetResponseDTO> addAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssetRequestDTO request) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(assetService.addAsset(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AssetResponseDTO>> getAllAssets(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(assetService.getAssetsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> getAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(assetService.getAssetById(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> updateAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequestDTO request) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(assetService.updateAsset(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        assetService.deleteAsset(userId, id);
        return ResponseEntity.noContent().build();
    }

}
