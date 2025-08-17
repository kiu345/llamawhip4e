package com.github.kiu345.eclipse.eclipseai.preferences;

public class AIProviderProfile {
    private String name;
    private AiProvider provider;
    private String urlBase;
    private String apiPath;
    private String modelApiPath;
    private String apiKey;
    private int connectTimeout;
    private int requestTimeout;
    private int keepAlive;
    private String organization;
    private String endpoint;

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AiProvider getProvider() {
        return provider;
    }

    public void setProvider(AiProvider provider) {
        this.provider = provider;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getModelApiPath() {
        return modelApiPath;
    }

    public void setModelApiPath(String modelApiPath) {
        this.modelApiPath = modelApiPath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void defaults() {
        switch (provider) {
            case GITHUB_COPILOT -> {
                urlBase = "";
                apiPath = "";
                modelApiPath = "";
                apiKey = "";
                connectTimeout = 30;
                requestTimeout = 90;
                keepAlive = 0;
                organization = "";
                endpoint = "";
            }
            case OLLAMA -> {
                urlBase = "http://localhost:11434";
                apiPath = "/api";
                modelApiPath = "/api/tags";
                apiKey = "ollama";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = 900;
                organization = "";
                endpoint = "";
            }
            case OPENAI -> {
                urlBase = "";
                apiPath = "";
                modelApiPath = "";
                apiKey = "";
                connectTimeout = 30;
                requestTimeout = 90;
                keepAlive = 0;
                organization = "";
                endpoint = "";
            }
            default -> {
                urlBase = "";
                apiPath = "";
                modelApiPath = "";
                apiKey = "";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = 900;
                organization = "";
                endpoint = "";
            }
        }
    }

    @Override
    public String toString() {
        return provider + ":" + name;
    }
}
