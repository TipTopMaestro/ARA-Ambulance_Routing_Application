package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "custom_logical_edges")
public class CustomLogicalEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(nullable = false)
    private double weight;

    public CustomLogicalEdge() {}

    public CustomLogicalEdge(String sourceId, String targetId, double weight) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.weight = weight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
