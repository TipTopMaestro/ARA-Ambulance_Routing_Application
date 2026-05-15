package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "hospitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "POINT", nullable = false)
    private Point location;
    
    // For MockDatabaseService compatibility
    public Hospital(Location loc, String name) {
        this.name = name;
        if (loc != null) {
            org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory();
            this.location = factory.createPoint(new org.locationtech.jts.geom.Coordinate(loc.getLongitude(), loc.getLatitude()));
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}
