package com.example.bellmanford.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public RouteController(BellmanFordService bellmanFordService, MockDatabaseService databaseService) {
        this.bellmanFordService = bellmanFordService;
        this.databaseService = databaseService;
    }

    @PostMapping("/path")
    public ResponseEntity<PathResponse> findShortestPath(@RequestBody PathRequest request) {
        PathResponse response = bellmanFordService.calculateShortestPath(request.getSourceId(), request.getTargetId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<GraphNode>> getNodes() {
        return ResponseEntity.ok(databaseService.getSignificantNodes());
    }
}