package com.example.yourapp.model;

public class ClimbingPlace {
    private String name;
    private String address;
    private String region;
    private String priceInfo;

    public ClimbingPlace(String name, String address, String region, String priceInfo) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.priceInfo = priceInfo;
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
}
