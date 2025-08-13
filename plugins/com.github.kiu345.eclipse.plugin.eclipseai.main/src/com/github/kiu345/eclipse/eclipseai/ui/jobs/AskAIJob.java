package com.github.kiu345.eclipse.eclipseai.ui.jobs;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.config.AIProviderProfile;
import com.github.kiu345.eclipse.eclipseai.config.ChatSettings;
import com.github.kiu345.eclipse.eclipseai.messaging.AgentMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.ConversationManager;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg.Source;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.github.kiu345.eclipse.eclipseai.ui.ChatComposite;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * A {@link Job} that sends a chat request to the configured AI provider and
 * updates the UI with the ongoing progress. The job handles the different
 * stages of the request (thinking, tool usage, responding) and updates the
 * {@link ChatComposite} accordingly. It also records timings and supports
 * cancellation.
 */
public class AskAIJob extends Job {

    private static final int UPDATE_PACKET_SIZE = 64;
    private static final int INITIAL_CHAT_BUFFER = 1024;

    private static final int STATE_ERROR = -1;
    private static final int STATE_STARTED = 0;
    private static final int STATE_THINKING = 1;
    private static final int STATE_TOOLS = 2;
    private static final int STATE_RESPONDING = 3;
    private static final int STATE_DONE = 4;

    private final ILog log;
    private final ChatAdapter<? extends StreamingChatModel> adapter;
    private final ModelDescriptor selectedModel;
    private final ConversationManager conversation;
    private final ChatComposite view;

    private int state = STATE_STARTED;
    private CompletableFuture<IStatus> future;
    private int responseCounter = 0;
    private UUID responeMessageId = UUID.randomUUID();

    private Instant start = null;;

    public AskAIJob(
            ILog log,
            ChatComposite view,
            ModelDescriptor selectedModel,
            ConversationManager conversation,
            ToolService toolService,
            ChatSettings settings,
            AIProviderProfile profile
    ) {
        super("Asking AI...");
        this.log = log;
        this.view = view;
        this.selectedModel = selectedModel;
        this.conversation = conversation;

        adapter = ChatAdapterFactory.create(log, profile);
        adapter.apply(settings);
        if (settings.getToolsAllowed()) {
            adapter.setToolService(toolService);
        }
    }

    public UUID getResponeMessageId() {
        return responeMessageId;
    }

    public void setResponeMessageId(UUID responeMessageId) {
        this.responeMessageId = responeMessageId;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (selectedModel == null) {
            return Status.error("invalid model selected");
        }
        monitor.beginTask("Preparing question...", IProgressMonitor.UNKNOWN);

        view.createElement(responeMessageId, Source.AGENT);

        monitor.setTaskName("Sending message...");
        log.info("sending request to " + selectedModel);
        StringBuilder thinkBuilder = new StringBuilder(INITIAL_CHAT_BUFFER);
        StringBuilder responseBuilder = new StringBuilder(INITIAL_CHAT_BUFFER);
        start = Instant.now();
        future = adapter.chatRequest(selectedModel, conversation::addLast, conversation.messages())
                .onError(this::handleError)
                .onMessageSend(() -> {
                    view.updateElement(responeMessageId, Msg.Type.MESSAGE, "...");
                    monitor.setTaskName("Waiting for response...");
                })
                .onPartialThinkingResponse((e) -> {
                    thinkBuilder.append(e);
                    if (state != STATE_THINKING) {
                        state = STATE_THINKING;
                        monitor.setTaskName("Thinking...");
                    }
                })
                .onPartialResponse((e) -> {
                    responseBuilder.append(e);
                    responseCounter += e.length();
                    if (state != STATE_RESPONDING) {
                        view.updateElement(responeMessageId, Msg.Type.MESSAGE, buildChatString(thinkBuilder.toString(), "..."));
                        state = STATE_RESPONDING;
                        monitor.setTaskName("Responding...");
                    }
                    else {
                        if (responseCounter > UPDATE_PACKET_SIZE) {
                            String response = responseBuilder.toString();
                            int lastNl = response.lastIndexOf("\n");
                            response = response.substring(0, Math.max(0, lastNl));
                            view.updateElement(responeMessageId, Msg.Type.MESSAGE, buildChatString(thinkBuilder.toString(), response));
                            responseCounter = 0;
                        }
                    }

                })
                .onPartialToolCallConsumer((e) -> {
                    if (state != STATE_TOOLS) {
                        state = STATE_TOOLS;
                        monitor.setTaskName("Using tools...".formatted(e.name()));
                    }
                })
                .onCompleteResponse(e -> handleCompleteResponse(monitor, e))
                .exec();
        try {
            IStatus result = future.join();
            monitor.done();
            return result;
        }
        catch (CancellationException ex) {
            monitor.done();
            return Status.CANCEL_STATUS;
        }
    }

    private String buildChatString(String thinking, String response) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(thinking)) {
            builder.append("<think>\n");
            builder.append(thinking);
            builder.append("</think>\n");
        }
        builder.append(response);
        return builder.toString();
    }

    private void handleError(Throwable e) {
        state = STATE_ERROR;
        log.error(e.getMessage(), e);
    }

    private void handleCompleteResponse(IProgressMonitor monitor, ChatResponse response) {
        state = STATE_DONE;
        AgentMsg msg = new AgentMsg(responeMessageId, response.aiMessage().text(), response.aiMessage().thinking());
        Instant end = Instant.now();
        msg.setTimings(start.until(end, ChronoUnit.MILLIS) * 0.001d);
        log.info("response took  %.2fs".formatted(msg.getTimings()));
        conversation.addLast(msg);
        view.updateElement(msg);
        view.addInputElement();
        monitor.setTaskName("Done");

    }

    @Override
    protected void canceling() {
        if (adapter != null) {
            adapter.cancelRequests();
        }
        if (future != null) {
            future.cancel(false);
        }
        super.canceling();
    }

}
