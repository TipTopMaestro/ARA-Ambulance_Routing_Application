package com.example.bellmanford.model;

import java.util.Map;

public class RelaxationStep {
    private int iteration;
    private String sourceId;
    private String targetId;
    private double weight;
    private double newDistance;
    private boolean relaxed;
    private Map<String, Double> currentDistances;

    public RelaxationStep(int iteration, String sourceId, String targetId, double weight, double newDistance,
            boolean relaxed, Map<String, Double> currentDistances) {
        this.iteration = iteration;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.weight = weight;
        this.newDistance = newDistance;
        this.relaxed = relaxed;
        this.currentDistances = currentDistances;
    }

    public int getIteration() {
        return iteration;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public double getWeight() {
        return weight;
    }

    public double getNewDistance() {
        return newDistance;
    }

    public boolean isRelaxed() {
        return relaxed;
    }

    public Map<String, Double> getCurrentDistances() {
        return currentDistances;
    }
}
