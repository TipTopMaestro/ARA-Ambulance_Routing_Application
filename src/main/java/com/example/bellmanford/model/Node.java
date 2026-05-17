package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    @Id
    @Column(name = "osm_node_id")
    private Long id;

    @Column(columnDefinition = "POINT", nullable = false)
    private Point location;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}
