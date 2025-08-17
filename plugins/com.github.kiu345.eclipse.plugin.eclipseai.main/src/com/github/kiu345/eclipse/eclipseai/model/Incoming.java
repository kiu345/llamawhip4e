package com.github.kiu345.eclipse.eclipseai.model;

public record Incoming(Type type, String payload, boolean append) {
    public enum Type {
        CONTENT,
        FUNCTION_CALL,
        ERROR
    }
    
    public Incoming(Type type, String payload) {
        this(type, payload, false);
    }
    
}
