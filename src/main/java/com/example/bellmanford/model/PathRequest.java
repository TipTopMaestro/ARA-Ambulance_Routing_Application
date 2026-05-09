package com.example.bellmanford.model;

public class PathRequest {
    private String sourceId;
    private String targetId;

    public PathRequest() {
    }

    public PathRequest(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}