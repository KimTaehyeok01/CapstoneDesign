package com.example.capstonedesign;

public class VisitedPlace {
    private String name;
    private String address;
    private String imageUrl;
    private String region;

    public VisitedPlace(String name, String address, String imageUrl, String region) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.region = region;
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

    public String getRegion() {
        return region;
    }
}