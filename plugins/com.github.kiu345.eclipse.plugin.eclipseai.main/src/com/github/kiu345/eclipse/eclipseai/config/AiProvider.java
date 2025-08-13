package com.github.kiu345.eclipse.eclipseai.config;

public enum AiProvider {
    OLLAMA("ollama", "Ollama", "Lokale oder Remote LLMs über Ollama API", "https://ollama.com"),
    LOCALAI("localai", "LocalAI", "On-Premise AI API Server", "https://localai.io"),
    OPENAI("openai", "OpenAI ChatGPT", "OpenAI API für GPT-Modelle", "https://chatgpt.com"),
    GITHUB_COPILOT("github", "GitHub Copilot", "GitHub Copilot für KI-unterstütztes Coding", "https://github.com/copilot");

    private final String internalName;
    private final String displayName;
    private final String description;
    private final String url;

    AiProvider(String internalName, String displayName, String description, String url) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
        this.url = url;
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

    public String getUrl() {
        return url;
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
