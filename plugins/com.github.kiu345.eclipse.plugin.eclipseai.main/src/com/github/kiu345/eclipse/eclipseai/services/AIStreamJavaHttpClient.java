package com.github.kiu345.eclipse.eclipseai.services;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;
import com.github.kiu345.eclipse.eclipseai.model.Conversation;
import com.github.kiu345.eclipse.eclipseai.model.Incoming;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;
import com.github.kiu345.eclipse.eclipseai.services.fixes.ChatFixer;
import com.github.kiu345.eclipse.eclipseai.services.fixes.OllamaQwenFixer;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService.ToolInfo;
import com.google.common.collect.Lists;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.inject.Inject;

/**
 * A Java HTTP client for streaming requests to OpenAI API.
 * This class allows subscribing to responses received from the OpenAI API and processes the chat completions.
 */
@Creatable
public class AIStreamJavaHttpClient {
    interface Assistant {
        String chat(String message);
    }

    private class StreamResponseHandler implements StreamingChatResponseHandler {

        private ChatFixer[] messageFixer = { new OllamaQwenFixer() };

        private StringBuilder storage;
        @SuppressWarnings("unused")
        private Throwable error;
        private final ChatRequest request;
        private final OllamaStreamingChatModel model;
        private final ModelDescriptor modelInfo;

        public StreamResponseHandler(ChatRequest request, OllamaStreamingChatModel model, ModelDescriptor modelInfo) {
            this.request = request;
            this.model = model;
            this.modelInfo = modelInfo;
            resetBuffer();
        }

        @Override
        public void onPartialResponse(String partialResponse) {
            if (partialResponse != null) {
//                System.out.print(partialResponse);
                storage.append(partialResponse);
            }
            if (isCancelled.get()) {
                throw new RuntimeException("interrupted");
            }
        }

        public String getBuffer() {
            return storage.toString();
        }

        public void resetBuffer() {
            storage = new StringBuilder(1024);
        }

        private ChatResponse analyzeResponse(ChatResponse response) {
            String data = getBuffer();
            if (response == null) {
                return response;
            }
            for (var fixer : messageFixer) {
                try {
                    response = fixer.process(modelInfo, response, data);
                }
                catch (Exception e) {
                    logger.info("not able to map to function call, using element as text block: " + e.getMessage());
                }

            }
            return response;
        }

        @Override
        public void onCompleteResponse(ChatResponse response) {
            if (response == null) {
                publisher.submit(new Incoming(Incoming.Type.CONTENT, "NULL"));
                logger.warn("response is null");
                return;
            }

            logger.info("Received response of type " + response.aiMessage().type().name());
            if (isCancelled.get()) {
                logger.info("Canceling");
                return;
            }
            response = analyzeResponse(response);

            AiMessage aiMessage = response.aiMessage();

            List<dev.langchain4j.data.message.ChatMessage> allMessages = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
            allMessages.addAll(request.messages());
            allMessages.add(aiMessage);

            List<ToolInfo> tools = toolService.findTools(configuration.getWebAccess().orElse(false));
            if (response.aiMessage() != null && response.aiMessage().hasToolExecutionRequests()) {
                for (ToolExecutionRequest execRequest : response.aiMessage().toolExecutionRequests()) {
                    if (isCancelled.get()) {
                        logger.info("Canceling");
                        break;
                    }
                    String toolResultValue = "";

                    try {
                        toolResultValue = toolService.executeTool(tools, execRequest);
                        if (toolResultValue == null) {
                            toolResultValue = null;
                            publisher.submit(new Incoming(Incoming.Type.CONTENT, "[Tool executed]", true));
                        }
                    }
                    catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(toolResultValue)) {
                        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(execRequest, toolResultValue);
                        allMessages.add(toolExecutionResultMessage);
                    }
                }

                switch (response.aiMessage().type()) {
                    case AI:
                        publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer() + "\n..."));
                        break;
                    case SYSTEM:
                        publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer() + "\n..."));
                        break;
                    default:
                        logger.info("unknown message type received, using as chat message: " + response.aiMessage().type());
                        publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer() + "\n..."));
                        break;
                }

                ChatRequest newRequest = createRequest(allMessages, modelInfo);
                StreamResponseHandler handler = new StreamResponseHandler(newRequest, model, modelInfo);
                model.doChat(newRequest, handler);
            }
            else {
                if (response.aiMessage() != null) {
                    switch (response.aiMessage().type()) {
                        case AI:
                            publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer()));
                            break;
                        case SYSTEM:
                            publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer()));
                            break;
                        default:
                            publisher.submit(new Incoming(Incoming.Type.CONTENT, getBuffer()));
                            break;
                    }
                }
                else {
                    logger.info("no aiMessage returned");
                }
                publisher.close();

            }
        }

        @Override
        public void onError(Throwable error) {
            logger.error(error.getMessage(), error);
            publisher.submit(new Incoming(Incoming.Type.ERROR, error.getMessage()));
            this.error = error;
            publisher.close();
        }

    }

    private SubmissionPublisher<Incoming> publisher;

    private Supplier<Boolean> isCancelled = () -> false;

    @Inject
    private ILog logger;

    @Inject
    private ClientConfiguration configuration;

    @Inject
    private ToolService toolService;

    private IPreferenceStore preferenceStore;

    public AIStreamJavaHttpClient() {
        preferenceStore = Activator.getDefault().getPreferenceStore();
        publisher = new SubmissionPublisher<>();
    }

    public void setCancelProvider(Supplier<Boolean> isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * Subscribes a given Flow.Subscriber to receive String data from OpenAI API responses.
     * 
     * @param subscriber the Flow.Subscriber to be subscribed to the publisher
     */
    public synchronized void subscribe(Flow.Subscriber<Incoming> subscriber) {
        publisher.subscribe(subscriber);
    }

    private dev.langchain4j.data.message.ChatMessage toChatMessage(ChatMessage message) {
        switch (message.role) {
            case ChatMessage.ROLE_SYSTEM:
                return new SystemMessage(message.getContent());
            case ChatMessage.ROLE_AI:
                return new AiMessage(message.getContent());
            default:
                return new UserMessage(message.getContent());
        }
    }

    private ChatRequest createRequest(List<dev.langchain4j.data.message.ChatMessage> messages, ModelDescriptor desciption) {
        OllamaChatRequestParameters.Builder paramsBuilder = OllamaChatRequestParameters.builder();

        String sysPrompt = preferenceStore.getString(Prompts.SYSTEM.preferenceName());

        Integer keepAlive = configuration.getKeepAliveSeconds();
        if (keepAlive != null) {
            paramsBuilder.keepAlive(keepAlive);
        }

        paramsBuilder.modelName(desciption.model());
        if (configuration.getUseFunctions().orElse(false)) {
            paramsBuilder.toolSpecifications(toolService.findTools(configuration.getWebAccess().orElse(false)).stream().map(e -> e.getTool()).toList());
        }

        return ChatRequest.builder()
                .messages(Stream.concat(Lists.newArrayList(new SystemMessage(sysPrompt)).stream(), messages.stream()).toList())
                .parameters(paramsBuilder.build())
                .build();
    }

    /**
     * Creates and returns a Runnable that will execute the HTTP request to OpenAI API
     * with the given conversation prompt and process the responses.
     * <p>
     * Note: this method does not block and the returned Runnable should be executed
     * to perform the actual HTTP request and processing.
     *
     * @param prompt the conversation to be sent to the OpenAI API
     * @return a Runnable that performs the HTTP request and processes the responses
     */
    public Runnable run(Conversation prompt) {
        return () -> {

            var modelName = configuration.getSelectedModel().orElseThrow();
            var modelInfo = configuration.getModelDescriptor(modelName).orElseThrow();

            OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                    .baseUrl(modelInfo.baseUri())
//                    .apiKey(modelInfo.apiKey())
                    .modelName(modelInfo.model())
                    .timeout(Duration.ofSeconds(configuration.getRequestTimoutSeconds()))
                    .temperature((configuration.getTemperature().orElse(4) / 10d))
                    .build();

            publisher.submit(new Incoming(Incoming.Type.CONTENT, "..."));

            ChatRequest request = createRequest(prompt.messages().stream().map(this::toChatMessage).toList(), modelInfo);
            StreamResponseHandler handler = new StreamResponseHandler(request, model, modelInfo);
            model.doChat(request, handler);
            /*
             * logger.info("Received response of type " + response.aiMessage().type().name());
             * // response.aiMessage().toolExecutionRequests().getFirst().
             * while (response.aiMessage().hasToolExecutionRequests()) {
             * if (isCancelled.get()) {
             * logger.info("Canceling");
             * break;
             * }
             * logger.info("Tool exec request detected");
             * AiMessage aiMessage = response.aiMessage();
             * 
             * List<dev.langchain4j.data.message.ChatMessage> allMessages = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
             * allMessages.addAll(request.messages());
             * allMessages.add(aiMessage);
             * 
             * List<ToolInfo> tools = toolService.findTools();
             * for (ToolExecutionRequest execRequest : response.aiMessage().toolExecutionRequests()) {
             * if (isCancelled.get()) {
             * logger.info("Canceling");
             * break;
             * }
             * String toolResultValue = "";
             * 
             * try {
             * toolResultValue = toolService.executeTool(tools, execRequest);
             * if (toolResultValue == null) {
             * logger.warn("Tool returned null");
             * toolResultValue = "";
             * }
             * }
             * catch (IOException e) {
             * logger.warn(e.getMessage(), e);
             * continue;
             * }
             * ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(execRequest, toolResultValue);
             * 
             * allMessages.add(toolExecutionResultMessage);
             * }
             * logger.info("resending request");
             * 
             * request = createRequest(allMessages, modelInfo, configuration.getUseFunctions().orElse(false));
             * response = model.doChat(request);
             * }
             * 
             * switch (response.aiMessage().type()) {
             * case AI:
             * publisher.submit(new Incoming(Incoming.Type.CONTENT, response.aiMessage().text()));
             * break;
             * case SYSTEM:
             * publisher.submit(new Incoming(Incoming.Type.CONTENT, response.aiMessage().text()));
             * break;
             * default:
             * publisher.submit(new Incoming(Incoming.Type.CONTENT, response.aiMessage().text()));
             * break;
             * }
             */
//            publisher.close();
        };
    }

}
