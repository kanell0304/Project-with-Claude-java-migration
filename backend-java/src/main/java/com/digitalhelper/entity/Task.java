package com.digitalhelper.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String keywords;

    public Task() {}

    public Task(String name, String displayName, String keywords) {
        this.name = name;
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getKeywords() { return keywords; }
}
