package com.example.bellmanford.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_paths")
public class GeneratedPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_location_id", nullable = false)
    private Location sourceLocation;

    @ManyToOne
    @JoinColumn(name = "target_location_id", nullable = false)
    private Location targetLocation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String routeJson;

    private Double estimatedDistanceMeters;
    private Double estimatedTimeSeconds;

    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    public GeneratedPath() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Location getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(Location sourceLocation) { this.sourceLocation = sourceLocation; }

    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location targetLocation) { this.targetLocation = targetLocation; }

    public String getRouteJson() { return routeJson; }
    public void setRouteJson(String routeJson) { this.routeJson = routeJson; }

    public Double getEstimatedDistanceMeters() { return estimatedDistanceMeters; }
    public void setEstimatedDistanceMeters(Double estimatedDistanceMeters) { this.estimatedDistanceMeters = estimatedDistanceMeters; }

    public Double getEstimatedTimeSeconds() { return estimatedTimeSeconds; }
    public void setEstimatedTimeSeconds(Double estimatedTimeSeconds) { this.estimatedTimeSeconds = estimatedTimeSeconds; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
