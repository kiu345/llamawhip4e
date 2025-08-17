package com.github.kiu345.eclipse.eclipseai.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.LoggerFactory;

import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.messaging.ToolsMsg;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService.ToolInfo;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * Base class for vendor chat adapters
 */
public abstract class ChatAdapterBase {
    /**
     * Fluent API implementation to create and execute a chat request
     * 
     * @param <T> the vendor stream chat model type
     */
    public class ChatCall<T extends StreamingChatModel> {
        private final ILog log;
        private final T chatModel;
        private final List<ChatMessage> messageList;
        private CompletableFuture<IStatus> future;

        private Consumer<Throwable> errorConsumer = e -> {
            LoggerFactory.getLogger(getClass()).error(e.getMessage());
        };

        private final Consumer<Msg> newMessageConsumer;

        private Runnable sendHandler;
        private Consumer<String> partialResponseConsumer;
        private Consumer<String> partialThinkingResponseConsumer;
        private Consumer<PartialToolCall> partialToolCallConsumer;
        private Consumer<CompleteToolCall> completeToolCallConsumer;
        private Consumer<ChatResponse> completeResponseConsumer = r -> {};

        /**
         * 
         * @param chatModel
         * @param log
         * @param messageList
         * @param newMessageConsumer consumer that is called if new chat messages (like tool requests) are created while processing the call
         */
        public ChatCall(T chatModel, ILog log, List<ChatMessage> messages, Consumer<Msg> newMessageConsumer) {
            this.chatModel = chatModel;
            this.messageList =Collections.synchronizedList(new ArrayList<>(Objects.requireNonNull(messages)));
            this.log = log;
            this.newMessageConsumer = Objects.requireNonNull(newMessageConsumer);
        }

        public ChatCall<T> onMessageSend(Runnable sendHandler) {
            this.sendHandler = sendHandler;
            return this;
        }

        public ChatCall<T> onError(Consumer<Throwable> errorConsumer) {
            this.errorConsumer = errorConsumer;
            return this;
        }

        public ChatCall<T> onPartialToolCallConsumer(Consumer<PartialToolCall> partialToolCallConsumer) {
            System.out.println("ChatAdapterBase.ChatCall.onPartialToolCallConsumer()");
            this.partialToolCallConsumer = partialToolCallConsumer;
            return this;
        }

        public ChatCall<T> onCompleteToolCall(Consumer<CompleteToolCall> completeToolCallConsumer) {
            System.out.println("ChatAdapterBase.ChatCall.onCompleteToolCall()");
            this.completeToolCallConsumer = completeToolCallConsumer;
            return this;
        }

        public ChatCall<T> onPartialResponse(Consumer<String> partialResponseConsumer) {
            this.partialResponseConsumer = partialResponseConsumer;
            return this;
        }

        public ChatCall<T> onPartialThinkingResponse(Consumer<String> partialThinkingResponseConsumer) {
            this.partialThinkingResponseConsumer = partialThinkingResponseConsumer;
            return this;
        }

        public ChatCall<T> onCompleteResponse(Consumer<ChatResponse> completeResponseConsumer) {
            this.completeResponseConsumer = Objects.requireNonNull(completeResponseConsumer);
            return this;
        }

        /**
         * Executes the chat request
         */
        public CompletableFuture<IStatus> exec() {
            future = new CompletableFuture<IStatus>();
            ChatAdapterBase.this.exec(chatModel, messageList, this, new ChatStreamResponseHandler<T>(this, newMessageConsumer));
            if (sendHandler != null) {
                sendHandler.run();
            }
            return future;
        }
    }

    protected class ChatStreamResponseHandler<T extends StreamingChatModel> implements StreamingChatResponseHandler {

        private final ChatCall<T> call;
        private final Consumer<Msg> newMessageConsumer;

        public ChatStreamResponseHandler(ChatCall<T> call, Consumer<Msg> newMessageConsumer) {
            this.call = call;
            this.newMessageConsumer = newMessageConsumer;
        }

        @Override
        public void onError(Throwable error) {
            if (call.errorConsumer != null) {
                call.errorConsumer.accept(error);
            }
            call.future.complete(Status.error(error.getMessage(), error));
        }

        @Override
        public void onPartialResponse(String partialResponse) {
            if (call.partialResponseConsumer != null) {
                call.partialResponseConsumer.accept(partialResponse);
            }
        }

        @Override
        public void onPartialThinking(PartialThinking partialThinking) {
            if (call.partialThinkingResponseConsumer != null) {
                call.partialThinkingResponseConsumer.accept(partialThinking.text());
            }
        }

        @Override
        public void onPartialToolCall(PartialToolCall partialToolCall) {
            if (call.partialToolCallConsumer != null) {
                call.partialToolCallConsumer.accept(partialToolCall);
            }
        }

        @Override
        public void onCompleteToolCall(CompleteToolCall completeToolCall) {
            if (call.completeToolCallConsumer != null) {
                call.completeToolCallConsumer.accept(completeToolCall);
            }
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            if (completeResponse.aiMessage() != null && completeResponse.aiMessage().hasToolExecutionRequests()) {
                if (call.future.isCancelled()) {
                    return;
                }

                final List<ChatMessage> newMessages = new ArrayList<>();
                final List<ToolsMsg> newHistoryMessages = new ArrayList<>();
                handleToolRequests(call.log, call.future, completeResponse.aiMessage().toolExecutionRequests(), (e) -> {
                    newMessages.add(e);
                    newHistoryMessages.add( new ToolsMsg(e));
                });
                newHistoryMessages.forEach(toolMessage -> newMessageConsumer.accept(toolMessage));
                call.messageList.addAll(newMessages);
                ChatAdapterBase.this.exec(call.chatModel, call.messageList, call, this);
                return;
            }
            call.completeResponseConsumer.accept(completeResponse);

            call.future.complete(Status.OK_STATUS);
        }

        protected void handleToolRequests(ILog log, CompletableFuture<IStatus> future, List<ToolExecutionRequest> requests, Consumer<ToolExecutionResultMessage> resultConsumer) {
            log.info("handling tool requests");
            var toolService = getToolService();
            List<ToolInfo> tools = toolService.findTools(false);
            for (ToolExecutionRequest execRequest : requests) {
                if (future.isCancelled()) {
                    log.info("Canceling");
                    break;
                }
                log.info("tool request: "+execRequest);
                
                String toolResultValue = "";

                try {
                    toolResultValue = toolService.executeTool(tools, execRequest);
                    if (toolResultValue == null) {
                        log.info("tool returned null");
                    }
                }
                catch (IOException e) {
                    log.warn(e.getMessage(), e);
                    continue;
                }
                if (StringUtils.isNotEmpty(toolResultValue)) {
                    ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(execRequest, toolResultValue);
                    resultConsumer.accept(toolExecutionResultMessage);
                }
            }

        }
    }

    abstract public ToolService getToolService();

    abstract protected void exec(StreamingChatModel model, List<ChatMessage> messageList, ChatCall<?> handler, StreamingChatResponseHandler responseHandler);
    
    abstract public void cancelRequests();
}
