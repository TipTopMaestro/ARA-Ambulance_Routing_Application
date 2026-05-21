package com.example.bellmanford.exception;

public class NegativeCycleException extends RuntimeException {
    public NegativeCycleException(String message) {
        super(message);
    }
}
