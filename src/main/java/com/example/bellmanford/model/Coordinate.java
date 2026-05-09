package com.example.bellmanford.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinate {
    @JsonProperty("lat")
    private double lat;
    @JsonProperty("lng")
    private double lng;

    public Coordinate() {
    }

    public Coordinate(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
