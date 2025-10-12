package com.example.capstonedesign;

public class VisitedPlace {
    private String name;
    private String address;
    private String imageUrl;
    // 참고: 현재 DB 구조상 날짜 데이터는 없으므로, 필요시 DB 구조 변경이 필요합니다.

    public VisitedPlace(String name, String address, String imageUrl) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}