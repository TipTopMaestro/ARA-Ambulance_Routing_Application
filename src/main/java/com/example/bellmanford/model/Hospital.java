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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", referencedColumnName = "id")
    private RouteNode routeNode;

    public Hospital() {}

    public Hospital(Long id, String name, RouteNode routeNode) {
        this.id = id;
        this.name = name;
        this.routeNode = routeNode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public RouteNode getRouteNode() { return routeNode; }
    public void setRouteNode(RouteNode routeNode) { this.routeNode = routeNode; }
}
