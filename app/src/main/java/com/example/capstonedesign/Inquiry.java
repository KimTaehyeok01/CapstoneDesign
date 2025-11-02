package com.example.capstonedesign;

import com.google.firebase.Timestamp;

public class Inquiry {
    private String title;
    private String content;
    private String status;
    private Timestamp timestamp;

    public Inquiry() {} // Firestore를 위한 빈 생성자

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
}