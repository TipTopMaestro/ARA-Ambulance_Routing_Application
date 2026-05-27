package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "route_nodes")
public class RouteNode {
    @Id
    private String id; // e.g., "BN1", "RE1", "H1"

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private String name;

    public RouteNode() {}

    public RouteNode(String id, double latitude, double longitude, String name) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
