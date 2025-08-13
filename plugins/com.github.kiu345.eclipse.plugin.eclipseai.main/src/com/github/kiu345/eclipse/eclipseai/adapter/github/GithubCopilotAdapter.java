package com.github.kiu345.eclipse.eclipseai.adapter.github;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.NotImplementedException;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterBase;
import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.config.AIProviderProfile;
import com.github.kiu345.eclipse.eclipseai.config.ChatSettings;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.github.GitHubModelsChatModelName;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;

public class GithubCopilotAdapter extends ChatAdapterBase implements ChatAdapter<GitHubModelsStreamingChatModel> {

    public static final String DEFAULT_URL = "http://localhost:11434";

    private String apiBaseAddress;
    private String apiKey;

    public GithubCopilotAdapter(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public GithubCopilotAdapter(AIProviderProfile provider) {
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
                .allOf(GitHubModelsChatModelName.class)
                .stream()
                .map(e -> new ModelDescriptor(DEFAULT_URL, e.toString(), apiKey, "github", e.name(), Collections.emptySet()))
                .toList();
        return result;
    }

    public StreamingChatModel getChat(String modelName) {
//        return GitHubModelsChatModel().builder()
//                .build();
        throw new NotImplementedException();
    }

    @Override
    public ChatCall<GitHubModelsStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Collection<Msg> messages) {
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
