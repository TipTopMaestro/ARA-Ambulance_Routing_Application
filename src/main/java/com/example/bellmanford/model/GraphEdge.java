package com.example.bellmanford.model;

public class GraphEdge {
    private final String source;
    private final String target;
    private final double baseTravelTime;
    private double trafficMultiplier;
    private double weightOffset = 0.0; // Can be negative for "boosters"

    public GraphEdge(String source, String target, double baseTravelTime, double trafficMultiplier) {
        this.source = source;
        this.target = target;
        this.baseTravelTime = baseTravelTime;
        this.trafficMultiplier = trafficMultiplier;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public double getBaseTravelTime() {
        return baseTravelTime;
    }

    public double getTrafficMultiplier() {
        return trafficMultiplier;
    }

    public void setTrafficMultiplier(double trafficMultiplier) {
        this.trafficMultiplier = trafficMultiplier;
    }

    public double getWeightOffset() {
        return weightOffset;
    }

    public void setWeightOffset(double weightOffset) {
        this.weightOffset = weightOffset;
    }

    public double getWeight() {
        return (baseTravelTime * trafficMultiplier) + weightOffset;
    }
}