package com.example.capstonedesign;

public class Achievement {
    private int badgeResId;
    private String title;
    private String description;
    private String date;

    public Achievement(int badgeResId, String title, String description, String date) {
        this.badgeResId = badgeResId;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // Getter 메서드들
    public int getBadgeResId() { return badgeResId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
}