package com.github.kiu345.eclipse.eclipseai.adapter.openai;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterBase;
import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.config.AIProviderProfile;
import com.github.kiu345.eclipse.eclipseai.config.ChatSettings;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class OpenAIAdapter extends ChatAdapterBase implements ChatAdapter<OpenAiStreamingChatModel> {

    public static final String DEFAULT_URL = "https://api.openai.com";

    private String apiBaseAddress;
    private String apiKey;

    public OpenAIAdapter(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public OpenAIAdapter(AIProviderProfile provider) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void apply(ChatSettings settings) {
        // TODO Auto-generated method stub
        
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
        List<ModelDescriptor> result = EnumSet
                .allOf(OpenAiChatModelName.class)
                .stream()
                .map(e -> new ModelDescriptor(DEFAULT_URL, e.toString(), apiKey, "openai", e.name(), Collections.emptySet()))
                .toList();
        return result;
    }

    @Override
    public ChatCall<OpenAiStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Collection<Msg> messages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void exec(StreamingChatModel model, List<ChatMessage> messageList, ChatCall<?> handler, StreamingChatResponseHandler responseHandler) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Msg generate(ModelDescriptor model, String request) {
        // TODO Auto-generated method stub
        return null;
    }

}
