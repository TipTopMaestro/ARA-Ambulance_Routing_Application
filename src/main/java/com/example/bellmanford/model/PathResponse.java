package com.example.bellmanford.model;

import java.util.List;

public class PathResponse {
    private final List<String> path;
    private final List<Coordinate> coordinates;
    private final double estimatedTime;
    private final boolean negativeCycleDetected;
    private final String message;
    private final List<RouteStep> steps;
    private final List<RelaxationStep> relaxationSteps;
    private Long pathId;

    public PathResponse(List<String> path, List<Coordinate> coordinates, double estimatedTime, boolean negativeCycleDetected, String message, List<RouteStep> steps, List<RelaxationStep> relaxationSteps) {
        this.path = path;
        this.coordinates = coordinates;
        this.estimatedTime = estimatedTime;
        this.negativeCycleDetected = negativeCycleDetected;
        this.message = message;
        this.steps = steps;
        this.relaxationSteps = relaxationSteps;
    }

    public List<String> getPath() {
        return path;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public boolean isNegativeCycleDetected() {
        return negativeCycleDetected;
    }

    public String getMessage() {
        return message;
    }

    public List<RouteStep> getSteps() {
        return steps;
    }

    public List<RelaxationStep> getRelaxationSteps() {
        return relaxationSteps;
    }

    public Long getPathId() {
        return pathId;
    }

    public void setPathId(Long pathId) {
        this.pathId = pathId;
    }
}
