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

    private final com.example.bellmanford.repository.GeneratedPathRepository generatedPathRepository;
    private final com.example.bellmanford.repository.LocationRepository locationRepository;

    public RouteController(BellmanFordService bellmanFordService, 
                           MockDatabaseService databaseService,
                           com.example.bellmanford.repository.GeneratedPathRepository generatedPathRepository,
                           com.example.bellmanford.repository.LocationRepository locationRepository) {
        this.bellmanFordService = bellmanFordService;
        this.databaseService = databaseService;
        this.generatedPathRepository = generatedPathRepository;
        this.locationRepository = locationRepository;
    }

    @PostMapping("/path")
    public ResponseEntity<PathResponse> findShortestPath(@RequestBody PathRequest request) {
        PathResponse response = bellmanFordService.calculateShortestPath(request.getSourceId(), request.getTargetId());
        
        // Log the path to the database
        try {
            com.example.bellmanford.model.Location source = locationRepository.findByOsmNodeId(request.getSourceId())
                .orElseGet(() -> {
                    GraphNode node = databaseService.getNode(request.getSourceId());
                    if (node == null) return null;
                    return locationRepository.save(new com.example.bellmanford.model.Location(
                        request.getSourceId(), node.getName(), node.getLatitude(), node.getLongitude(), 
                        com.example.bellmanford.model.Location.LocationType.NODE
                    ));
                });

            com.example.bellmanford.model.Location target = locationRepository.findByOsmNodeId(request.getTargetId())
                .orElseGet(() -> {
                    GraphNode node = databaseService.getNode(request.getTargetId());
                    if (node == null) return null;
                    return locationRepository.save(new com.example.bellmanford.model.Location(
                        request.getTargetId(), node.getName(), node.getLatitude(), node.getLongitude(), 
                        com.example.bellmanford.model.Location.LocationType.NODE
                    ));
                });

            if (source != null && target != null && !response.getPath().isEmpty()) {
                com.example.bellmanford.model.GeneratedPath pathLog = new com.example.bellmanford.model.GeneratedPath();
                pathLog.setSourceLocation(source);
                pathLog.setTargetLocation(target);
                pathLog.setEstimatedDistanceMeters(0.0); // Should be calculated if needed
                pathLog.setEstimatedTimeSeconds(response.getEstimatedTime());
                // Simple JSON serialization for now
                pathLog.setRouteJson(response.getPath().toString()); 
                generatedPathRepository.save(pathLog);
            }
        } catch (Exception e) {
            System.err.println("Failed to log path: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<GraphNode>> getNodes() {
        return ResponseEntity.ok(databaseService.getSignificantNodes());
    }
}