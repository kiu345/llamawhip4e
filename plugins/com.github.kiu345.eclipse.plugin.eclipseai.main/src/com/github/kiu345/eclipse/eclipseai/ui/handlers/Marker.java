package com.github.kiu345.eclipse.eclipseai.ui.handlers;

public record Marker(
        String type,
        String line,
        String token,
        String message) {}
