package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.NewsItemDto;
import com.crypto.portfolio.dto.NewsResponseDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class NewsService {

    private final String NEWS_API_URL = "https://min-api.cryptocompare.com/data/v2/news/?lang=EN";
    private final RestTemplate restTemplate = new RestTemplate();

    // In-memory cache
    private List<NewsItemDto> cachedNews = new ArrayList<>();
    private LocalDateTime lastCacheUpdate = LocalDateTime.now();

    @PostConstruct
    public void init() {
        fetchNewsFromApi();
    }

    @Scheduled(fixedRate = 300000) // Refresh every 5 minutes (300,000 ms)
    public void scheduleNewsRefresh() {
        fetchNewsFromApi();
    }

    public NewsResponseDto getLatestNews() {
        return new NewsResponseDto(cachedNews, lastCacheUpdate);
    }

    private void fetchNewsFromApi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    NEWS_API_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("Data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("Data");
                List<NewsItemDto> newItems = new ArrayList<>();

                for (Map<String, Object> item : data) {
                    // Extract fields safely
                    String id = String.valueOf(item.get("id"));
                    String title = (String) item.get("title");
                    String bodyText = (String) item.get("body"); // Often short description
                    String url = (String) item.get("url");
                    String imageUrl = (String) item.get("imageurl");
                    String source = (String) item.get("source");

                    long publishedOn = 0;
                    Object pubObj = item.get("published_on");
                    if (pubObj instanceof Number) {
                        publishedOn = ((Number) pubObj).longValue();
                    }

                    // Basic validation
                    if (title != null && !title.isEmpty()) {
                        NewsItemDto dto = new NewsItemDto(id, title, bodyText, source, url, imageUrl, publishedOn);
                        newItems.add(dto);
                    }
                }

                // If successful updates
                if (!newItems.isEmpty()) {
                    this.cachedNews = newItems;
                    this.lastCacheUpdate = LocalDateTime.now();
                    System.out.println("News Cache Updated: " + newItems.size() + " items at " + lastCacheUpdate);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch news: " + e.getMessage());
            // Keep old cache if fetch fails
        }
    }
}
