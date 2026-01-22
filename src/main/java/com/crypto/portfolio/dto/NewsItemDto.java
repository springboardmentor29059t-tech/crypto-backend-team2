package com.crypto.portfolio.dto;

public class NewsItemDto {
    private String id;
    private String title;
    private String summary;
    private String source;
    private String url;
    private String imageUrl;
    private long publishedAt;

    public NewsItemDto() {
    }

    public NewsItemDto(String id, String title, String summary, String source, String url, String imageUrl,
            long publishedAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.source = source;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }
}
