package com.example.bellmanford.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphNode {
    private final String id;
    private final double latitude;
    private final double longitude;
    private String name;

    @JsonCreator
    public GraphNode(
            @JsonProperty("id") String id, 
            @JsonProperty("latitude") double latitude, 
            @JsonProperty("longitude") double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GraphNode(String id, double latitude, double longitude, String name) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
