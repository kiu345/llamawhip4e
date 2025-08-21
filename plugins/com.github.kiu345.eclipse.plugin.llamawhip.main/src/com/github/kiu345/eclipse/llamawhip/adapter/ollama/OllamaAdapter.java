package com.github.kiu345.eclipse.llamawhip.adapter.ollama;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapter;
import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterBase;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor.Features;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;
import com.github.kiu345.eclipse.llamawhip.config.ChatSettings;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.AgentMsg;
import com.github.kiu345.eclipse.llamawhip.messaging.Msg;
import com.github.kiu345.eclipse.llamawhip.messaging.SystemMsg;
import com.github.kiu345.eclipse.llamawhip.messaging.ToolsMsg;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.google.common.collect.Lists;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaModel;
import dev.langchain4j.model.ollama.OllamaModelCard;
import dev.langchain4j.model.ollama.OllamaModels;
import dev.langchain4j.model.ollama.OllamaModels.OllamaModelsBuilder;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;

/**
 * Ollama REST API adapter
 */
public class OllamaAdapter extends ChatAdapterBase implements ChatAdapter<OllamaStreamingChatModel> {

    public static class Config {
        private String baseUrl = DEFAULT_URL;
        private String systemPrompt = "You are a helpful assistant";

        public static final int DEFAULT_TEMP = 30;
        public static final int DEFAULT_REPEAT_PENALTY = 120;

        private Boolean thinkingAllowed = null;

        private Integer temperature = DEFAULT_TEMP;
        private Integer repeatPenalty = DEFAULT_REPEAT_PENALTY;

        private Integer connectTimeout = 10;
        private Integer timeout = 60;
        private Integer keepAlive = 900;

        public Config() {
        }

        public Config(AIProviderProfile profile) {
            this.baseUrl = profile.getUrlBase();
            this.connectTimeout = profile.getConnectTimeout();
            this.timeout = profile.getRequestTimeout();
            this.keepAlive = profile.getKeepAlive();
        }

        public Config(String baseUrl) {
            super();
            this.baseUrl = baseUrl;
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

        public Optional<Integer> getTemperature() {
            return Optional.ofNullable(temperature);
        }

        public void setTemperature(Integer temperature) {
            this.temperature = temperature;
        }

        public Optional<Integer> getRepeatPenalty() {
            return Optional.ofNullable(repeatPenalty);
        }

        public void setRepeatPenalty(Integer repeatPenalty) {
            this.repeatPenalty = repeatPenalty;
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

        public Integer getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(Integer keepAlive) {
            this.keepAlive = keepAlive;
        }
    }

    public static final String DEFAULT_URL = "http://localhost:11434";
    private static final double DIVIDER = 100d;
    private static int MAX_RETRIES = 3;
    private static int MODEL_LOAD_TIMEOUT = 15;

    private final Config config;
    private final ILog log;

    public OllamaAdapter(ILog log, AIProviderProfile provider) {
        this(log, new Config(provider));
        String sysPrompt = PluginConfiguration.instance().getPrompt(Prompts.SYSTEM);
        if (StringUtils.isNotBlank(sysPrompt)) {
            config().setSystemPrompt(sysPrompt);
        }
    }

    public OllamaAdapter(ILog log, Config clientConfig) {
        this.config = clientConfig;
        this.log = log;
    }

    private OllamaAdapter(AIProviderProfile provider) {
        this(null, new Config(provider));
    }

    /**
     * Validates the settings
     * 
     * @return a array of error messages, or a empty list if there are none
     */
    public static String[] validate(AIProviderProfile profile) {
        if (profile == null) {
            return new String[] { "Internal error, profile is null" };
        }

        try {
            var adapterInstance = new OllamaAdapter(profile);
            adapterInstance.getModels();
        }
        catch (Exception ex) {
            return new String[] { ex.getClass().getSimpleName() + ": " + ex.getMessage() };
        }

        return new String[] {};
    }

    @Override
    public String[] validate(ChatSettings settings) {
        return new String[] {};
    }

    @Override
    public void apply(ChatSettings settings) {
        config.setThinkingAllowed(settings.getThinkingAllowed());
        config.setTemperature(settings.getTemperatur());
        config.setRepeatPenalty(settings.getRepeatPenalty());
    }

    public synchronized List<ModelDescriptor> getModels() {
        ArrayList<ModelDescriptor> result = new ArrayList<>();

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_URL;

        var httpBuilder = new JdkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(MODEL_LOAD_TIMEOUT));

        OllamaModels modelsService = new OllamaModelsBuilder()
                .httpClientBuilder(httpBuilder)
                .baseUrl(baseUrl)
                .maxRetries(MAX_RETRIES)
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
            if (log != null) {
                log.error(e.getMessage(), e);
            }
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
            String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
            String salutation = "informal";
            String language = "Deutsch";

            var loader = new PromptLoader(log);
            String baseMessage = loader.getBaseTemplate();
            String sysPrompt = config.getSystemPrompt();
            sysPrompt = loader.replaceVariables(baseMessage, date, language, salutation).replace(PromptLoader.TEMPLATE_PROMPT, sysPrompt);

            messageList = Lists.asList(SystemMessage.from(config.getSystemPrompt()), messageList.toArray(ChatMessage[]::new));
        }

        Boolean enableThinking = model.features().contains(Features.THINKING) ? config.isThinkingAllowed() : null;

        var httpBuilder = new JdkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(config.getTimeout()));

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_URL;

        var temperature = config.getTemperature();
        var repeatPenalty = config.getRepeatPenalty();

        Double temperatureVal = temperature.isPresent() ? temperature.get() / DIVIDER : null;
        Double repeatPenaltyVal = repeatPenalty.isPresent() ? repeatPenalty.get() / DIVIDER : null;

        var chatModel = OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .httpClientBuilder(httpBuilder)
                .modelName(model.model())
                .think(enableThinking)
                .returnThinking(true)
                .temperature(temperatureVal)
                .repeatPenalty(repeatPenaltyVal)
                .build();
        return new ChatCall<OllamaStreamingChatModel>(chatModel, log, messageList, newMessageConsumer);
    }

    @Override
    protected void exec(StreamingChatModel model, List<ChatMessage> messageList, ChatCall<?> handler, StreamingChatResponseHandler responseHandler) {
        var paramsBuilder = OllamaChatRequestParameters.builder()
                .keepAlive(config.getKeepAlive());
        if (getToolService() != null) {
            paramsBuilder.toolSpecifications(getToolService().findTools(false).stream().map(e -> e.getTool()).toList());
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

    @SuppressWarnings("unused")
    @Override
    public Msg generate(ModelDescriptor model, String request) {
        var httpBuilder = new JdkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(config.getTimeout()));

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_URL;

        var pluginConfig = PluginConfiguration.instance();

        var temperature = config.getTemperature();
        var repeatPenalty = config.getRepeatPenalty();
        var chatModel = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .httpClientBuilder(httpBuilder)
                .modelName(model.model())
                .think(pluginConfig.getCcAllowThinking())
                .returnThinking(false)
                .temperature(0.4)
                .repeatPenalty(1.2)
                .topP(0.8)
                .topK(100)
                .numPredict(1024)
                .timeout(Duration.ofSeconds(30))
                .build();

        ChatResponse response = chatModel
                .chat(SystemMessage.from("""
                        This is a code completion request form a IDE.
                        Create a code completion ideas for code at the location ${{CURSOR}} and an optioal ${{ENDCURSOR}}!
                        ${{ENDCURSOR}} marks the end of an text area if the user has selected a text block, it is missing otherwise.
                        Code will replace exactly that location, do never create code that is outside this area.
                        If requested to create documentation, do not add the source to it!

                        The context of the request is provided inside <|CONTEXT|> tags:
                        <|CONTEXT|>
                        package demo;

                        public class Demo {
                        ${{CURSOR}}
                        }

                        </|CONTEXT|>

                        Do NOT add  <|CONTEXT|>,  <|/CONTEXT|>, ${{CURSOR}} or ${{ENDCURSOR}} in the response!

                        Remember: Only return the code without any markup and no other messages!
                        The user can add a custom message at the end of the request, separated by:
                        ---
                                """), UserMessage.from(request));
        if (response != null) {
            return new AgentMsg(response.aiMessage().text());
        }
        else {
            throw new IllegalStateException("response is null");
        }
    }
}
