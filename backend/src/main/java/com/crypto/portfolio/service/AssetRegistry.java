package com.crypto.portfolio.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AssetRegistry {
    public static final Map<String, String> SYMBOL_TO_ID;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("BTC", "bitcoin");
        map.put("ETH", "ethereum");
        map.put("BNB", "binancecoin");
        map.put("SOL", "solana");
        map.put("XRP", "ripple");
        map.put("ADA", "cardano");
        map.put("DOGE", "dogecoin");
        map.put("USDT", "tether");
        map.put("USDC", "usd-coin");
        map.put("AVAX", "avalanche-2");
        map.put("DOT", "polkadot");
        map.put("TRX", "tron");
        map.put("LINK", "chainlink");
        map.put("MATIC", "matic-network");
        map.put("WBTC", "wrapped-bitcoin");
        map.put("UNI", "uniswap");
        map.put("LTC", "litecoin");
        map.put("DAI", "dai");
        map.put("BCH", "bitcoin-cash");
        map.put("ATOM", "cosmos");
        map.put("XLM", "stellar");
        map.put("SHIB", "shiba-inu");
        SYMBOL_TO_ID = Collections.unmodifiableMap(map);
    }
}
