package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.NewsResponseDto;
import com.crypto.portfolio.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "http://localhost:3000")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/latest")
    public ResponseEntity<NewsResponseDto> getLatestNews() {
        return ResponseEntity.ok(newsService.getLatestNews());
    }
}
