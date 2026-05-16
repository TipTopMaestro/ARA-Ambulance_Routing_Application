package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "osm_node_id", nullable = false)
    private Node node;

    public Hospital() {}

    public Hospital(Long id, String name, Node node) {
        this.id = id;
        this.name = name;
        this.node = node;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Node getNode() { return node; }
    public void setNode(Node node) { this.node = node; }
}
