package com.example.bellmanford.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.PathRequest;
import com.example.bellmanford.model.PathResponse;
import com.example.bellmanford.service.BellmanFordService;
import com.example.bellmanford.service.StaticGraphService;

import java.util.Collection;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class RouteController {

    private final BellmanFordService bellmanFordService;
    private final StaticGraphService graphService;

    public RouteController(BellmanFordService bellmanFordService, 
                           StaticGraphService graphService) {
        this.bellmanFordService = bellmanFordService;
        this.graphService = graphService;
    }

    @PostMapping("/path")
    public ResponseEntity<PathResponse> findShortestPath(@RequestBody PathRequest request) {
        PathResponse response = bellmanFordService.calculateShortestPath(request.getSourceId(), request.getTargetId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<Collection<GraphNode>> getNodes() {
        return ResponseEntity.ok(graphService.getNodes().values());
    }

    @GetMapping("/edges")
    public ResponseEntity<?> getEdges() {
        return ResponseEntity.ok(graphService.getEdges());
    }
}
