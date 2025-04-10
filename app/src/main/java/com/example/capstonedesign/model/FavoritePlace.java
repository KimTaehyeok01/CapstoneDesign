package com.example.capstonedesign.model;

public class FavoritePlace {
    private String name;
    private String address;
    private String region;
    private String priceInfo;
    private int imageResId;

    public FavoritePlace(String name, String address, String region, String priceInfo, int imageResId) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.priceInfo = priceInfo;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getRegion() {
        return region;
    }

    public String getPriceInfo() {
        return priceInfo;
    }

    public int getImageResId() {
        return imageResId;
    }
}
