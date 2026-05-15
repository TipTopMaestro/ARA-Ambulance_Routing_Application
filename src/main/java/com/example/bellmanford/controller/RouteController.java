package com.example.bellmanford.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.PathRequest;
import com.example.bellmanford.model.PathResponse;
import com.example.bellmanford.service.BellmanFordService;
import com.example.bellmanford.service.MockDatabaseService;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class RouteController {

    private final BellmanFordService bellmanFordService;
    private final MockDatabaseService databaseService;
    private final ObjectMapper objectMapper;

    public RouteController(BellmanFordService bellmanFordService, 
                           MockDatabaseService databaseService,
                           ObjectMapper objectMapper) {
        this.bellmanFordService = bellmanFordService;
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/path")
    public ResponseEntity<PathResponse> findShortestPath(@RequestBody PathRequest request) {
        PathResponse response = bellmanFordService.calculateShortestPath(request.getSourceId(), request.getTargetId());
        // Path persistence logic removed as requested by cleanup of redundant models.
        // Mission entity now handles path persistence via pathJson column.
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<GraphNode>> getNodes() {
        return ResponseEntity.ok(databaseService.getSignificantNodes());
    }
}
