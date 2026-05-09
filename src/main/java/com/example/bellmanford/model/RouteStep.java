package com.example.bellmanford.model;

public class RouteStep {
    private final String source;
    private final String target;
    private final double weight;

    public RouteStep(String source, String target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }
}
