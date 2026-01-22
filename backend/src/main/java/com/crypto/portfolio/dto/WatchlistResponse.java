package com.crypto.portfolio.dto;

import java.util.List;
import java.util.Map;

public class WatchlistResponse {
    private String dataSource;
    private String lastUpdated;
    private List<Map<String, Object>> assets;

    public WatchlistResponse(String dataSource, String lastUpdated, List<Map<String, Object>> assets) {
        this.dataSource = dataSource;
        this.lastUpdated = lastUpdated;
        this.assets = assets;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<Map<String, Object>> getAssets() {
        return assets;
    }

    public void setAssets(List<Map<String, Object>> assets) {
        this.assets = assets;
    }
}
