package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "POINT", nullable = false)
    private Point location;

    public Hospital() {}

    public Hospital(Long id, String name, Point location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}
