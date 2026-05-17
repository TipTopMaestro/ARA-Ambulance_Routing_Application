package com.example.bellmanford.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "edges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long edge_id;

    @ManyToOne
    @JoinColumn(name = "source_node_id")
    private Node source;

    @ManyToOne
    @JoinColumn(name = "target_node_id")
    private Node target;

    private Double distanceMeters;
    private Double trafficFactor;

    public double getWeight() {
        if (distanceMeters == null || trafficFactor == null) return 0.0;
        return distanceMeters * trafficFactor;
    }

    public Long getEdgeId() { return edge_id; }
    public void setEdgeId(Long edgeId) { this.edge_id = edgeId; }
    public Node getSource() { return source; }
    public void setSource(Node source) { this.source = source; }
    public Node getTarget() { return target; }
    public void setTarget(Node target) { this.target = target; }
    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }
    public Double getTrafficFactor() { return trafficFactor; }
    public void setTrafficFactor(Double trafficFactor) { this.trafficFactor = trafficFactor; }
}
