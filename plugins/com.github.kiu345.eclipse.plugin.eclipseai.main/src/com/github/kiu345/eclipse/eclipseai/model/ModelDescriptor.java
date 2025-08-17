package com.github.kiu345.eclipse.eclipseai.model;

import java.util.Set;

public record ModelDescriptor(
        String baseUri,
        String model,
        String apiKey,
        String provider,
        String name,
        Set<Features> features) {

    public enum Features {
        CHAT,
        THINKING,
        TOOLS,
        VISION,
        EMBEDDING
    }

    @Override
    public final String toString() {
        return model;
    }
}
