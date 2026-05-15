package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "locations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"latitude", "longitude"}),
    @UniqueConstraint(columnNames = {"osmNodeId"})
})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String osmNodeId;

    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

    public enum LocationType {
        HOSPITAL, PATIENT, NODE
    }

    public Location() {}

    public Location(String osmNodeId, String name, Double latitude, Double longitude, LocationType locationType) {
        this.osmNodeId = osmNodeId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationType = locationType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOsmNodeId() { return osmNodeId; }
    public void setOsmNodeId(String osmNodeId) { this.osmNodeId = osmNodeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }
}
