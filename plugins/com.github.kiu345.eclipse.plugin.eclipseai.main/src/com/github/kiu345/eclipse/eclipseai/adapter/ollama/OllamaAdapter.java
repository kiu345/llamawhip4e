package com.github.kiu345.eclipse.eclipseai.adapter.ollama;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.stream.Streams;
import org.eclipse.core.runtime.ILog;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterBase;
import com.github.kiu345.eclipse.eclipseai.messaging.AgentMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.messaging.SystemMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.ToolsMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.UserMsg;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor.Features;
import com.github.kiu345.eclipse.eclipseai.services.ClientConfiguration;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.google.common.collect.Lists;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaModel;
import dev.langchain4j.model.ollama.OllamaModelCard;
import dev.langchain4j.model.ollama.OllamaModels;
import dev.langchain4j.model.ollama.OllamaModels.OllamaModelsBuilder;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;

public class OllamaAdapter extends ChatAdapterBase implements ChatAdapter {

    public static class Config {
        private String baseUrl = DEFAULT_URL;
        private Integer temperature;
        private String systemPrompt = "You are a helpful assistant";
        private Boolean thinkingAllowed = null;
        private int connectTimeout = 10;
        private int timeout = 60;
        private int keepAlive = 900;

        public Config() {
        }

        public Config(ClientConfiguration config) {
            this(config.getApiBaseUrl(), config.getTemperature().orElse(null));
        }

        public Config(String baseUrl, Integer temperature) {
            super();
            this.baseUrl = baseUrl;
            this.temperature = temperature;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Integer getTemperature() {
            return temperature;
        }

        public void setTemperature(Integer temperature) {
            this.temperature = temperature;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public Boolean isThinkingAllowed() {
            return thinkingAllowed;
        }

        public void setThinkingAllowed(Boolean thinkingAllowed) {
            this.thinkingAllowed = thinkingAllowed;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(int keepAlive) {
            this.keepAlive = keepAlive;
        }
    }

    public static final String DEFAULT_URL = "http://localhost:11434";
    public static final int DEFAULT_TEMP = 4;

    private final Config config;
    private final ILog log;
    private ToolService toolService;

    public OllamaAdapter(ILog log, Config clientConfig) {
        this.config = clientConfig;
        this.log = log;
    }

    public ToolService getToolService() {
        return toolService;
    }

    public void setToolService(ToolService toolservice) {
        this.toolService = toolservice;
    }

    public synchronized List<ModelDescriptor> getModels() {
        System.out.println("OllamaAdapter.getModels()");
        ArrayList<ModelDescriptor> result = new ArrayList<>();

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_URL;

        OllamaModels modelsService = new OllamaModelsBuilder()
                .httpClientBuilder(new JdkHttpClientBuilder())
                .baseUrl(baseUrl)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(3)
                .logRequests(true)
                .logResponses(true)
                .build();

        try {
            Response<List<OllamaModel>> response = modelsService.availableModels();
            List<OllamaModel> models = response.content();
            if (models != null) {
                for (OllamaModel model : models) {
                    OllamaModelCard card = modelsService.modelCard(model).content();
                    final boolean function = card.getTemplate().contains("{{ .Function.Name }}");
                    final boolean thinking = card.getTemplate().contains("{{ .Thinking }}") || card.getTemplate().contains("{{ .ThinkLevel }}");
                    Set<ModelDescriptor.Features> features = new HashSet<>();

                    card.getCapabilities().stream()
                            .filter(e -> StringUtils.isNotBlank(e))
                            .map(e -> e.toLowerCase()).forEach(e -> {
                                switch (e) {
                                    case "completion":
                                        features.add(Features.CHAT);
                                        break;
                                    case "thinking":
                                        if (thinking) {
                                            features.add(Features.THINKING);
                                        }
                                        break;
                                    case "vision":
                                        features.add(Features.VISION);
                                        break;
                                    case "tools":
                                        if (function) {
                                            features.add(Features.TOOLS);
                                        }
                                        break;
                                    case "embedding":
                                        features.add(Features.EMBEDDING);
                                        break;
                                }
                            });

                    ModelDescriptor modelDesc = new ModelDescriptor(
                            baseUrl, model.getModel(), null, "ollama", model.getName(), features
                    );
                    result.add(modelDesc);
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public Config config() {
        return config;
    }

    public ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, String message) {
        return chatRequest(model, newMessageConsumer, UserMessage.from(message));
    }

    public ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Msg... messages) {
        var converted = Streams.of(messages).map(e -> mapMsg(e)).toArray(ChatMessage[]::new);
        return chatRequest(model, newMessageConsumer, converted);
    }

    public ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Collection<Msg> messages) {
        var converted = messages.stream().map(e -> mapMsg(e)).toArray(ChatMessage[]::new);
        return chatRequest(model, newMessageConsumer, converted);
    }

    private ChatMessage mapMsg(Msg input) {
        return switch (input) {
            case SystemMsg msg:
                yield new SystemMessage(input.getMessage());
            case UserMsg msg:
                yield new UserMessage(input.getMessage());
            case AgentMsg msg:
                yield new AiMessage(input.getMessage());
            case ToolsMsg msg:
                yield new ToolExecutionResultMessage(msg.getRequestId(), msg.getToolName(), msg.getMessage());
            default:
                throw new IllegalArgumentException("Unsupported Msg type: " + input.getClass());
        };
    }

    public ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, ChatMessage... messages) {

        List<ChatMessage> messageList = Arrays.asList(messages);
        long sysMsgCount = messageList.stream().filter(SystemMessage.class::isInstance).count();
        if (sysMsgCount > 1) {
            throw new IllegalStateException("more than one system message found");
        }

        if (sysMsgCount == 0 && config.getSystemPrompt() != null) {
            messageList = Lists.asList(SystemMessage.from(config.getSystemPrompt()), messageList.toArray(ChatMessage[]::new));
        }

        Boolean enableThinking = model.features().contains(Features.THINKING) ? config.isThinkingAllowed() : null;

        var httpBuilder = new JdkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(config.getTimeout()));

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_URL;

        Optional<Integer> temperature = Optional.ofNullable(config.temperature);
        var chatModel = OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .httpClientBuilder(httpBuilder)
                .modelName(model.model())
                .think(enableThinking)
                .returnThinking(true)
                .temperature(temperature.orElse(DEFAULT_TEMP) / 10d)
                .build();
        return new ChatCall<OllamaStreamingChatModel>(chatModel, log, messageList, newMessageConsumer);
    }

    @Override
    protected void exec(StreamingChatModel model, List<ChatMessage> messageList, ChatCall<?> handler, StreamingChatResponseHandler responseHandler) {
        var paramsBuilder = OllamaChatRequestParameters.builder()
                .keepAlive(config.getKeepAlive());
        if (toolService != null) {
            paramsBuilder.toolSpecifications(toolService.findTools(false).stream().map(e -> e.getTool()).toList());
        }
        var request = ChatRequest.builder()
                .messages(messageList)
                .parameters(paramsBuilder.build())
                .build();

        model.chat(request, responseHandler);
    }

    @Override
    public void cancelRequests() {

    }
}
