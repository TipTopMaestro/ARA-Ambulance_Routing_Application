package com.example.bellmanford.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.bellmanford.model.Coordinate;
import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.PathResponse;
import com.example.bellmanford.model.RouteStep;

@Service
public class BellmanFordService {

    private final MockDatabaseService databaseService;

    public BellmanFordService(MockDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public PathResponse calculateShortestPath(String sourceId, String targetId) {
        List<GraphEdge> edges = databaseService.getEdges();
        Map<String, GraphNode> nodes = databaseService.getNodes();

        Set<String> vertices = new HashSet<>();
        for (GraphEdge edge : edges) {
            vertices.add(edge.getSource());
            vertices.add(edge.getTarget());
        }
        vertices.add(sourceId);
        vertices.add(targetId);

        Map<String, Double> distance = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();
        List<com.example.bellmanford.model.RelaxationStep> relaxationSteps = new ArrayList<>();

        for (String vertex : vertices) {
            distance.put(vertex, Double.POSITIVE_INFINITY);
            predecessor.put(vertex, null);
        }
        distance.put(sourceId, 0.0);

        int vertexCount = vertices.size();

        for (int i = 0; i < vertexCount - 1; i++) {
            boolean relaxedAny = false;
            for (GraphEdge edge : edges) {
                double sourceDistance = distance.getOrDefault(edge.getSource(), Double.POSITIVE_INFINITY);
                if (sourceDistance != Double.POSITIVE_INFINITY) {
                    double newDistance = sourceDistance + edge.getWeight();
                    boolean shouldRelax = newDistance < distance.getOrDefault(edge.getTarget(), Double.POSITIVE_INFINITY);
                    
                    if (shouldRelax) {
                        distance.put(edge.getTarget(), newDistance);
                        predecessor.put(edge.getTarget(), edge.getSource());
                        relaxedAny = true;
                    }
                    
                    // Only record significant relaxations to avoid bloat.
                    // We cap the total relaxation steps to prevent memory exhaustion in cycle scenarios.
                    if (shouldRelax && relaxationSteps.size() < 5000) {
                        relaxationSteps.add(new com.example.bellmanford.model.RelaxationStep(
                            i, edge.getSource(), edge.getTarget(), edge.getWeight(), newDistance, shouldRelax, null
                        ));
                    }
                }
            }
            if (!relaxedAny) {
                break;
            }
        }

        boolean negativeCycleDetected = false;
        for (GraphEdge edge : edges) {
            double sourceDistance = distance.getOrDefault(edge.getSource(), Double.POSITIVE_INFINITY);
            if (sourceDistance != Double.POSITIVE_INFINITY && sourceDistance + edge.getWeight() < distance.getOrDefault(edge.getTarget(), Double.POSITIVE_INFINITY)) {
                negativeCycleDetected = true;
                break;
            }
        }

        List<String> path = reconstructPath(predecessor, sourceId, targetId);
        String message;
        if (negativeCycleDetected) {
            message = "Negative cycle detected; distances may not be stable.";
        } else if (path.isEmpty()) {
            message = "Target unreachable from the source.";
        } else {
            message = "Shortest path calculated successfully.";
        }

        List<RouteStep> steps = new ArrayList<>();
        for (GraphEdge edge : edges) {
            steps.add(new RouteStep(edge.getSource(), edge.getTarget(), edge.getWeight()));
        }

        List<Coordinate> coordinates = new ArrayList<>();
        for (String nodeId : path) {
            GraphNode node = nodes.get(nodeId);
            if (node != null) {
                coordinates.add(new Coordinate(node.getLatitude(), node.getLongitude()));
            }
        }

        double estimatedTime = path.isEmpty() ? Double.POSITIVE_INFINITY : distance.getOrDefault(targetId, Double.POSITIVE_INFINITY);
        return new PathResponse(path, coordinates, estimatedTime, negativeCycleDetected, message, steps, relaxationSteps);
    }

    private List<String> reconstructPath(Map<String, String> predecessor, String sourceId, String targetId) {
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = targetId;
        while (current != null) {
            if (visited.contains(current)) {
                // Cycle detected in predecessor map! Break to avoid infinite loop.
                break;
            }
            visited.add(current);
            path.add(0, current);
            if (current.equals(sourceId)) {
                break;
            }
            current = predecessor.get(current);
        }
        if (path.isEmpty() || !path.get(0).equals(sourceId)) {
            return new ArrayList<>();
        }
        return path;
    }
}