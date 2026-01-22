package com.crypto.portfolio.dto;

import java.util.List;
import java.util.Map;

public class MarketDataResult {
    private List<Map<String, Object>> data;
    private String dataSource; // LIVE, CACHED, FALLBACK
    private String timestamp;

    public MarketDataResult(List<Map<String, Object>> data, String dataSource, String timestamp) {
        this.data = data;
        this.dataSource = dataSource;
        this.timestamp = timestamp;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
