package com.crypto.portfolio.dto;

import java.util.List;
import java.time.LocalDateTime;

public class NewsResponseDto {
    private List<NewsItemDto> news;
    private LocalDateTime lastUpdated;

    public NewsResponseDto(List<NewsItemDto> news, LocalDateTime lastUpdated) {
        this.news = news;
        this.lastUpdated = lastUpdated;
    }

    public List<NewsItemDto> getNews() {
        return news;
    }

    public void setNews(List<NewsItemDto> news) {
        this.news = news;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
