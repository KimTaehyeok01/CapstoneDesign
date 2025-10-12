package com.example.capstonedesign;

// public 으로 선언하여 다른 클래스에서 접근할 수 있도록 합니다.
public class Achievement {
    private int badgeResId;
    private String title;
    private String description;
    private String date;

    // Firestore 라이브러리가 데이터를 객체로 자동 변환하려면,
    // 반드시 비어있는 기본 생성자가 필요합니다.
    public Achievement() {}

    // Activity에서 데이터를 채워넣을 때 사용하는 생성자
    public Achievement(int badgeResId, String title, String description, String date) {
        this.badgeResId = badgeResId;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // Adapter에서 데이터를 가져다 쓰기 위한 Getter 메서드들
    public int getBadgeResId() {
        return badgeResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}