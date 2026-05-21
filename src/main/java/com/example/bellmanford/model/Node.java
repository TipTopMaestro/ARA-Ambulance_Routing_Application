package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;

public class Node {
    private Long id;
    private Point location;

    public Node() {}

    public Node(Long id, Point location) {
        this.id = id;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}
