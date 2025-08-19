package com.github.kiu345.eclipse.llamawhip.config;

import java.util.Objects;
import java.util.UUID;

public class AIProviderProfile {
    private UUID id = UUID.randomUUID();
    private String name = "";
    private AIProvider provider;
    private String urlBase;
    private String apiPath;
    private String modelPath;
    private String modelNames;
    private String apiKey;
    private int connectTimeout = 15;
    private int requestTimeout = 60;
    private Integer keepAlive;
    private String organization;
    private String endpoint;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = Objects.requireNonNull(id);
    }

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
        this.name = Objects.requireNonNull(name);
    }

    public AIProvider getProvider() {
        return provider;
    }

    public void setProvider(AIProvider provider) {
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

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelApiPath) {
        this.modelPath = modelApiPath;
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

    public Integer getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void defaults() {
        switch (provider) {
//            case GITHUB_COPILOT -> {
//                urlBase = "";
//                apiPath = "";
//                modelApiPath = "";
//                apiKey = "";
//                connectTimeout = 30;
//                requestTimeout = 90;
//                keepAlive = null;
//                organization = "";
//                endpoint = "";
//            }
            case OLLAMA -> {
                urlBase = "http://localhost:11434";
                apiPath = "/api";
                modelPath = "/api/tags";
                apiKey = "ollama";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = null;
                organization = "";
                endpoint = "";
            }
            case LOCALAI -> {
                urlBase = "http://localhost:8080";
                apiPath = "/v1";
                modelPath = "";
                apiKey = "localai";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = null;
                organization = "";
                endpoint = "";
            }
            case JLAMA -> {
                urlBase = "";
                apiPath = "";
                modelPath = "./models";
                apiKey = "";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = null;
                organization = "";
                endpoint = "";
            }
            case OPENAI -> {
                urlBase = "";
                apiPath = "";
                modelPath = "";
                apiKey = "";
                connectTimeout = 30;
                requestTimeout = 90;
                keepAlive = null;
                organization = "";
                endpoint = "";
            }
            default -> {
                urlBase = "";
                apiPath = "";
                modelPath = "";
                apiKey = "";
                connectTimeout = 15;
                requestTimeout = 180;
                keepAlive = null;
                organization = "";
                endpoint = "";
            }
        }
    }

    @Override
    public String toString() {
        return provider + ":" + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, apiPath, connectTimeout, endpoint, id, keepAlive, modelPath, modelNames, name, organization, provider, requestTimeout, urlBase);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AIProviderProfile)) {
            return false;
        }
        AIProviderProfile other = (AIProviderProfile) obj;
        return Objects.equals(apiKey, other.apiKey) && Objects.equals(apiPath, other.apiPath) && connectTimeout == other.connectTimeout && Objects.equals(endpoint, other.endpoint)
                && Objects.equals(id, other.id) && Objects.equals(keepAlive, other.keepAlive) && Objects.equals(modelPath, other.modelPath) && Objects.equals(modelNames, other.modelNames)
                && Objects.equals(name, other.name) && Objects.equals(organization, other.organization) && provider == other.provider && requestTimeout == other.requestTimeout
                && Objects.equals(urlBase, other.urlBase);
    }
}
