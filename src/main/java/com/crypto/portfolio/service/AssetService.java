package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.AssetRequestDTO;
import com.crypto.portfolio.dto.AssetResponseDTO;
import com.crypto.portfolio.model.Asset;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.AssetRepository;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final MarketService marketService;
    private final com.crypto.portfolio.repository.TradeRepository tradeRepository;

    public AssetService(AssetRepository assetRepository, UserRepository userRepository, MarketService marketService,
            com.crypto.portfolio.repository.TradeRepository tradeRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.marketService = marketService;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public AssetResponseDTO addAsset(Long userId, AssetRequestDTO request) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.crypto.portfolio.exception.UserNotFoundException(
                        "User not found with ID: " + userId));

        // Check if asset already exists
        assetRepository.findByUserIdAndSymbol(userId, request.getSymbol())
                .ifPresent(a -> {
                    throw new RuntimeException("Asset with symbol " + request.getSymbol() + " already exists");
                });

        Asset asset = new Asset(
                user,
                request.getSymbol(),
                request.getName(),
                request.getQuantity(),
                request.getAvgBuyPrice(),
                request.getSource());

        Asset savedAsset = assetRepository.save(asset);

        // Record Initial BUY Trade
        recordTrade(user, asset.getSymbol(), "BUY", asset.getAmount().doubleValue(),
                asset.getAvgBuyPrice().doubleValue(), asset.getSource());

        return mapToDTO(savedAsset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponseDTO> getAssetsByUserId(Long userId) {
        return assetRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AssetResponseDTO> getAssetsPaginated(Long userId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return assetRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public AssetResponseDTO getAssetById(Long userId, Long assetId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(assetId, "assetId must not be null");
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new com.crypto.portfolio.exception.AssetNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (!asset.getUser().getId().equals(userId)) {
            throw new com.crypto.portfolio.exception.AssetNotFoundException("Asset not found for this user");
        }

        return mapToDTO(asset);
    }

    @Transactional
    public AssetResponseDTO updateAsset(Long userId, Long assetId, AssetRequestDTO request) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(assetId, "assetId must not be null");
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new com.crypto.portfolio.exception.AssetNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (!asset.getUser().getId().equals(userId)) {
            throw new com.crypto.portfolio.exception.AssetNotFoundException("Asset not found for this user");
        }

        BigDecimal oldQty = asset.getAmount();
        BigDecimal newQty = request.getQuantity();

        // Record Trade Diff
        if (newQty.compareTo(oldQty) > 0) {
            // BUY
            BigDecimal diff = newQty.subtract(oldQty);
            recordTrade(asset.getUser(), asset.getSymbol(), "BUY", diff.doubleValue(),
                    request.getAvgBuyPrice().doubleValue(), request.getSource());
        } else if (newQty.compareTo(oldQty) < 0) {
            // SELL
            BigDecimal diff = oldQty.subtract(newQty);
            // Price for SELL currently unknown in Request, assume Current Market Price or
            // User provided 'price'.
            // But UpdateAsset DTO usually has 'AvgBuyPrice' which is COST BASIS updating.
            // For a SELL, the user implies they sold 'diff' amount.
            // We need a 'Separation of Concerns' maybe, but strictly for prompt: Use market
            // service price or avg price.
            // Let's use Request price as the "Execution Price" if logical, or just Avg
            // Price.
            // Ideally Update Asset = "Correction". But user requested "Live Tax".
            // We will log it as a SELL at current market price if available, else request
            // price.
            double sellPrice = marketService.getPriceBySymbol(request.getSymbol());
            if (sellPrice <= 0)
                sellPrice = request.getAvgBuyPrice().doubleValue();

            recordTrade(asset.getUser(), asset.getSymbol(), "SELL", diff.doubleValue(), sellPrice, request.getSource());
        }

        asset.setSymbol(request.getSymbol());
        asset.setName(request.getName());
        asset.setAmount(request.getQuantity());
        asset.setAvgBuyPrice(request.getAvgBuyPrice());
        asset.setSource(request.getSource());

        Asset updatedAsset = assetRepository.save(asset);
        return mapToDTO(updatedAsset);
    }

    @Transactional
    public void deleteAsset(Long userId, Long assetId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(assetId, "assetId must not be null");
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new com.crypto.portfolio.exception.AssetNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (!asset.getUser().getId().equals(userId)) {
            throw new com.crypto.portfolio.exception.AssetNotFoundException("Asset not found for this user");
        }

        // Record SELL of entire amount
        double sellPrice = marketService.getPriceBySymbol(asset.getSymbol());
        if (sellPrice <= 0)
            sellPrice = asset.getAvgBuyPrice().doubleValue();

        recordTrade(asset.getUser(), asset.getSymbol(), "SELL", asset.getAmount().doubleValue(), sellPrice,
                asset.getSource());

        assetRepository.delete(asset);
    }

    private void recordTrade(User user, String symbol, String side, double qty, double price, String exchange) {
        try {
            com.crypto.portfolio.model.Trade trade = new com.crypto.portfolio.model.Trade();
            trade.setUser(user);
            trade.setAssetSymbol(symbol);
            trade.setSide(side);
            trade.setQuantity(qty);
            trade.setPrice(price);
            trade.setExchange(exchange);
            trade.setExecutedAt(java.time.LocalDateTime.now());
            trade.setFee(0.0); // Default or calc
            tradeRepository.save(trade);
        } catch (Exception e) {
            System.err.println("Failed to record auto-trade: " + e.getMessage());
        }
    }

    private AssetResponseDTO mapToDTO(Asset asset) {
        double livePriceDouble = marketService.getPriceBySymbol(asset.getSymbol());
        BigDecimal currentPrice = livePriceDouble > 0 ? BigDecimal.valueOf(livePriceDouble) : asset.getAvgBuyPrice();
        BigDecimal currentValue = asset.getAmount().multiply(currentPrice);
        BigDecimal totalCost = asset.getAmount().multiply(asset.getAvgBuyPrice());
        BigDecimal profitLoss = currentValue.subtract(totalCost);
        BigDecimal profitLossPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return new AssetResponseDTO(
                asset.getId(),
                asset.getSymbol(),
                asset.getName(),
                asset.getAmount(),
                asset.getAvgBuyPrice(),
                currentValue,
                totalCost,
                profitLoss,
                profitLossPercentage,
                asset.getSource(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}
