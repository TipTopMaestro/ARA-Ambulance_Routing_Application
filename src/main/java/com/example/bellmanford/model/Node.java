package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private Long id;
    private Point location;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}
