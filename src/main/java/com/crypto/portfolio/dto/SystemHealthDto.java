package com.crypto.portfolio.dto;

import java.util.Map;

public class SystemHealthDto {
    private String status;
    private Map<String, String> components;

    public SystemHealthDto(String status, Map<String, String> components) {
        this.status = status;
        this.components = components;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, String> getComponents() {
        return components;
    }
}
