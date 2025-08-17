package com.github.kiu345.eclipse.eclipseai.preferences;

public enum AiProvider {
    OLLAMA("Ollama", "Ollama", "Lokale oder Remote LLMs über Ollama API"),
    OPENAI("OpenAI", "OpenAI", "OpenAI API für GPT-Modelle"),
    GITHUB_COPILOT("GitHub Copilot", "GitHub Copilot", "GitHub Copilot für KI-unterstütztes Coding");

    private final String internalName;
    private final String displayName;
    private final String description;

    AiProvider(String internalName, String displayName, String description) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AiProvider fromInternalName(String name) {
        for (AiProvider p : values()) {
            if (p.getInternalName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}
