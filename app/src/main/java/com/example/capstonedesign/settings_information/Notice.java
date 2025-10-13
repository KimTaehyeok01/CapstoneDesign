package com.example.capstonedesign.settings_information;

import com.google.firebase.Timestamp;

public class Notice {
    private String title;
    private String content;
    private Timestamp timestamp;

    public Notice() {
        // Firestore Deserialization을 위한 빈 생성자
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}