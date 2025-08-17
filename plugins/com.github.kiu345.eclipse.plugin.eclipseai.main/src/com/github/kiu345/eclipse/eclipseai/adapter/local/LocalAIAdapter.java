package com.github.kiu345.eclipse.eclipseai.adapter.local;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterBase.ChatCall;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

public class LocalAIAdapter implements ChatAdapter {
    
    public static final String DEFAULT_URL = "http://localhost:8082/v1";
    
    private String apiBaseAddress;
    private String apiKey;

    public LocalAIAdapter(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        if (apiBaseAddress == null) {
            throw new NullPointerException("Null not allowed for parameter apiBaseAddress");
        }
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<ModelDescriptor> getModels() {
        ArrayList<ModelDescriptor> result = new ArrayList<>();
        ModelDescriptor modelDesc = new ModelDescriptor(apiBaseAddress, "gpt-4", apiKey, "local", "gpt-4", Collections.emptySet());
        result.add(modelDesc);
        return result;
    }

    public ChatModel getChat(String modelName) {
        return LocalAiChatModel.builder()
                .baseUrl(apiBaseAddress)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .maxRetries(3)
                .timeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Collection<Msg> messages) {
        // TODO Auto-generated method stub
        return null;
    }
}
