package com.example.bellmanford.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.bellmanford.model.CustomLogicalEdge;
import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.Hospital;
import com.example.bellmanford.model.RouteNode;
import com.example.bellmanford.repository.CustomLogicalEdgeRepository;
import com.example.bellmanford.repository.HospitalRepository;
import com.example.bellmanford.repository.RouteNodeRepository;

import jakarta.annotation.PostConstruct;

/**
 * Database-driven Graph Provider.
 * All nodes and edges are fetched from the database.
 */
@Service
public class StaticGraphService {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new java.util.ArrayList<>();
    
    private final HospitalRepository hospitalRepository;
    private final RouteNodeRepository routeNodeRepository;
    private final CustomLogicalEdgeRepository customLogicalEdgeRepository;

    public StaticGraphService(HospitalRepository hospitalRepository,
                              RouteNodeRepository routeNodeRepository,
                              CustomLogicalEdgeRepository customLogicalEdgeRepository) {
        this.hospitalRepository = hospitalRepository;
        this.routeNodeRepository = routeNodeRepository;
        this.customLogicalEdgeRepository = customLogicalEdgeRepository;
    }

    @PostConstruct
    public void init() {
        refreshGraph();
    }

    /**
     * Loads the graph from the database. 
     */
    public void refreshGraph() {
        nodes.clear();
        edges.clear();

        // 1. Load Hospitals
        List<Hospital> hospitals = hospitalRepository.findAll();
        for (Hospital h : hospitals) {
            if (h.getRouteNode() != null) {
                addNode(h.getRouteNode().getId(), h.getRouteNode().getLatitude(), h.getRouteNode().getLongitude(), h.getName());
            }
        }

        // 2. Load other Route Nodes (Intersections, Residential Entrances, etc.)
        List<RouteNode> routeNodes = routeNodeRepository.findAll();
        for (RouteNode rn : routeNodes) {
            // Only add if not already added by hospitals (to prevent overwriting custom hospital names)
            if (!nodes.containsKey(rn.getId())) {
                addNode(rn.getId(), rn.getLatitude(), rn.getLongitude(), rn.getName());
            }
        }

        // 3. Load Custom Logical Edges
        List<CustomLogicalEdge> logicalEdges = customLogicalEdgeRepository.findAll();
        for (CustomLogicalEdge le : logicalEdges) {
            addEdge(le.getSourceId(), le.getTargetId(), le.getWeight());
        }
        
        System.out.println("Graph loaded from database: " + nodes.size() + " nodes, " + edges.size() + " edges.");
    }

    private void addNode(String id, double lat, double lon, String name) {
        GraphNode node = new GraphNode(id, lat, lon);
        node.setName(name);
        nodes.put(id, node);
    }

    private void addEdge(String source, String target, double weight) {
        edges.add(new GraphEdge(source, target, weight, 1.0));
    }

    public Map<String, GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
}
