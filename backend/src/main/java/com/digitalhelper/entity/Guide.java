package com.digitalhelper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guides", uniqueConstraints = @UniqueConstraint(columnNames = {"app_name", "task"}))
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_name", nullable = false)
    private String appName;

    @Column(nullable = false)
    private String task;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public Guide() {}

    public Guide(String appName, String task, String content, String sourceUrl, LocalDateTime expiresAt) {
        this.appName = appName;
        this.task = task;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getAppName() { return appName; }
    public String getTask() { return task; }
    public String getContent() { return content; }
    public String getSourceUrl() { return sourceUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setContent(String content) { this.content = content; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
