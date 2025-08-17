package com.github.kiu345.eclipse.eclipseai.ui;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.eclipseai.messaging.ConversationManager;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.messaging.UserMsg;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.services.ClientConfiguration;
import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService;
import com.github.kiu345.eclipse.eclipseai.ui.ChatComposite.State;
import com.github.kiu345.eclipse.eclipseai.ui.jobs.AskAIJob;

import jakarta.inject.Inject;

/**
 * Business logic of the chat widget
 */
public class ChatPresenter {
    /**
     * Chat UI settings storage
     */
    public static class Settings {
        private Boolean thinkingAllowed = true;
        private Boolean toolsAllowed = true;
        private Boolean webAllowed = false;
        private Integer temperatur = 1;

        public Settings() {
            super();
        }

        public Settings(Boolean thinkingAllowed, Boolean toolsAllowed, Boolean webAllowed, Integer temperatur) {
            super();
            this.thinkingAllowed = thinkingAllowed;
            this.toolsAllowed = toolsAllowed;
            this.webAllowed = webAllowed;
            this.temperatur = temperatur;
        }

        public Boolean getThinkingAllowed() {
            return thinkingAllowed;
        }

        public void setThinkingAllowed(Boolean thinkingAllowed) {
            this.thinkingAllowed = thinkingAllowed;
        }

        public Boolean getToolsAllowed() {
            return toolsAllowed;
        }

        public void setToolsAllowed(Boolean toolsAllowed) {
            this.toolsAllowed = toolsAllowed;
        }

        public Boolean getWebAllowed() {
            return webAllowed;
        }

        public void setWebAllowed(Boolean webAllowed) {
            this.webAllowed = webAllowed;
        }

        public Integer getTemperatur() {
            return temperatur;
        }

        public void setTemperatur(Integer temperatur) {
            this.temperatur = temperatur;
        }
    }

    /**
     * The view of this presenter
     */
    private final ChatComposite view;

    @Inject
    private ILog log;

    private ModelDescriptor selectedModel = null;

    @Inject
    private ConversationManager conversation;

    @Inject
    private ClientConfiguration configuration;

    @Inject
    private ToolService toolService;

    @Inject
    private ModelManager modelManager;

    private IPreferenceStore preferenceStore;

    private Job sendJob = null;

    private Settings settings = new Settings();

    public ChatPresenter(ChatComposite parent) {
        this.view = parent;
        preferenceStore = Activator.getDefault().getPreferenceStore();
    }

    public Settings getSettings() {
        return settings;
    }

    public ConversationManager getConversationManager() {
        return conversation;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        view.updateWith(settings);
    }

    public void doAttachFile() {
        System.out.println("ChatPresenter.onAttachFile()");
    }

    public void doSend(final UUID userMessageId, final String userPrompt, boolean predefinedPrompt) {
        final UserMsg message = new UserMsg(userMessageId, userPrompt);
        conversation.addLast(message);

        sendJob = new AskAIJob(
                log,
                view,
                selectedModel,
                conversation,
                toolService,
                settings,
                configuration,
                preferenceStore
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
                    var config = new OllamaAdapter.Config(configuration.getBaseUrl(), 3);
                    view.setModelList(modelManager.models(new OllamaAdapter(log, config)));
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
