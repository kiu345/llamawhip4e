package com.github.kiu345.eclipse.eclipseai.ui.pref;

public enum AIType {
    OLLAMA,
    OPENAI,
    LOCAL,
    GITHUB;

    @Override
    public String toString() {
        if (describeConstable().isPresent()) {
            switch (this) {
                case OLLAMA:
                    return "Ollama";
                case OPENAI:
                    return "ChatGPT";
                case LOCAL:
                    return "Local";
                case GITHUB:
                    return "GitHub";
                default:
                    throw new IllegalArgumentException("Unexpected value: " + describeConstable().get());
            }
        }
        return name();
    }
}
