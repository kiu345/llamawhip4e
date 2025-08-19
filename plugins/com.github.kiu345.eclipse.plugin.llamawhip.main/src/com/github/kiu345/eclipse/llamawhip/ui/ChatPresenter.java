package com.github.kiu345.eclipse.llamawhip.ui;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;

import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.github.kiu345.eclipse.eclipseai.ui.attachment.FileAttachment;
import com.github.kiu345.eclipse.eclipseai.ui.jobs.AskAIJob;
import com.github.kiu345.eclipse.eclipseai.ui.util.IDEUtils;
import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.config.ChatSettings;
import com.github.kiu345.eclipse.llamawhip.config.ModelManager;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.ConversationManager;
import com.github.kiu345.eclipse.llamawhip.messaging.Msg;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.ui.ChatComposite.State;

import jakarta.inject.Inject;

/**
 * The {@code ChatPresenter} class encapsulates the business logic for the chat widget
 * in the Eclipse AI plugin. It mediates between the UI component {@link ChatComposite}
 * and the underlying services such as {@link ConversationManager}, {@link ToolService},
 * and {@link ModelManager}. The presenter handles user actions (send, stop, clear,
 * resend, remove last, refresh, copy, and save code), manages the current model
 * selection, and coordinates asynchronous jobs to query the AI model via
 * {@link AskAIJob}. It also supports file attachments, converting them into
 * special markers in the prompt.
 */
public class ChatPresenter {
    /**
     * The view of this presenter
     */
    private final ChatComposite view;

    @Inject
    private ILog log;

    private ModelDescriptor selectedModel = null;

    @Inject
    private ConversationManager conversation;

    private PluginConfiguration configuration = PluginConfiguration.instance();

    @Inject
    private ToolService toolService;

    @Inject
    private ModelManager modelManager;

    private Job sendJob = null;

    private ChatSettings settings = new ChatSettings();

    private Set<FileAttachment> attachments = new HashSet<>(10);

    public ChatPresenter(ChatComposite parent) {
        this.view = parent;
    }

    public ChatSettings getSettings() {
        return settings;
    }

    public ConversationManager getConversationManager() {
        return conversation;
    }

    public void setSettings(ChatSettings settings) {
        this.settings = settings;
        view.updateWith(settings);
    }

    public void doAttachFile() {
        var currentFile = IDEUtils.getCurrentEditorFile();
        if (currentFile == null) {
            log.info("no file found");
            return;
        }
        doAttachFile(currentFile);
    }

    public void doAttachFile(IFile file) {
        FileAttachment attachment = new FileAttachment(file);
        attachments.add(attachment);
        view.setInputAttachments(attachments);
    }

    private String addAttachments(String input) {
        if (attachments.isEmpty()) {
            return input;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(input);
            for (FileAttachment attachment : attachments) {
                builder.append("\n<|FILE=%s|>\n".formatted(attachment.getShortName()));
                builder.append(new String(attachment.getFile().readAllBytes(), attachment.getFile().getCharset()));
                builder.append("\n</|FILE|>\n");
            }

            return builder.toString();
        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return input;
        }
    }

    public void doSend(final UUID userMessageId, final String userPrompt, boolean predefinedPrompt) {
        final UserMsg message = new UserMsg(userMessageId, addAttachments(userPrompt));
        conversation.addLast(message);

        sendJob = new AskAIJob(
                log,
                view,
                selectedModel,
                conversation,
                toolService,
                settings,
                configuration.getDefaultProfile()
        );

        sendJob.addJobChangeListener(new IJobChangeListener() {

            @Override
            public void sleeping(IJobChangeEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void scheduled(IJobChangeEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void running(IJobChangeEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void done(IJobChangeEvent event) {
                view.setButtonStates(State.FOLLOWUP);
//                view.addInputElement();
            }

            @Override
            public void awake(IJobChangeEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void aboutToRun(IJobChangeEvent event) {
                // TODO Auto-generated method stub

            }
        });
        attachments.clear();

        sendJob.schedule();
    }

    public void doStop() {
        if (sendJob != null) {
            sendJob.cancel();
        }
    }

    public void doClear() {
        doStop();
        conversation.clear();
        attachments.clear();
        view.clearChatView(true);
        view.setButtonStates(State.NEW);
    }

    public void doResend() {
        Optional<Msg> msg = conversation.lastMessage();
        if (!(msg.orElse(null) instanceof UserMsg)) {
            conversation.removeLast();
        }
        var lastMsg = conversation.removeLast();
        if (!msg.isEmpty()) {
            view.resetMessages(conversation.messages());
        }
        else {
            view.clearChatView(true);
        }

        if (lastMsg.isPresent() && lastMsg.get() instanceof UserMsg userMsg) {
            view.prepareDoSend(userMsg.getMessage(), false);
        }
        else {
            view.addInputElement();
        }
    }

    public void doRemoveLast() {
        Optional<Msg> msg = conversation.lastMessage();
        if (!(msg.orElse(null) instanceof UserMsg)) {
            conversation.removeLast();
        }
        var lastMsg = conversation.removeLast();
        if (!msg.isEmpty()) {
            view.resetMessages(conversation.messages());
            view.addInputElement();
        }
        else {
            view.clearChatView(true);
            view.setButtonStates(State.NEW);
        }

        if (lastMsg.isPresent() && lastMsg.get() instanceof UserMsg userMsg) {
            view.setInputText(userMsg.getMessage());
        }
    }

    public void doRefresh() {
        reloadModels();
    }

    public void doCopyCode(String codeBlock) {
        var clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
        var textTransfer = TextTransfer.getInstance();
        clipboard.setContents(new Object[] { codeBlock }, new Transfer[] { textTransfer });
        clipboard.dispose();
    }

    public void doSaveCode(String codeBlock, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(codeBlock);
        }
        catch (IOException e) {
            log.error("Error writing to file: " + e.getMessage());
        }
    }

    public ModelDescriptor getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(ModelDescriptor selectedModel) {
        this.selectedModel = selectedModel;
    }

    public void reloadModels() {
        Job job = new Job("Refreshing models") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Loading models...", IProgressMonitor.UNKNOWN);

                try {
                    if (configuration.getDefaultProfile() == null) {
                        log.warn("no default profile defined");
                        return Status.CANCEL_STATUS;
                    }
                    var adapter = ChatAdapterFactory.create(log, configuration.getDefaultProfile());
                    view.setModelList(modelManager.models(adapter));
                }
                catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    monitor.done();
                    return Status.error(ex.getMessage());

                }

                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
