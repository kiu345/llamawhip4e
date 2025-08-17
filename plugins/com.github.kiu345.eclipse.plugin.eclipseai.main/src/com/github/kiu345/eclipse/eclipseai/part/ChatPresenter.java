package com.github.kiu345.eclipse.eclipseai.part;

import static com.github.kiu345.eclipse.eclipseai.util.ImageUtilities.createPreview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.PlatformUI;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.handlers.EclipseAIHandlerInvoker;
import com.github.kiu345.eclipse.eclipseai.jobs.EclipseAIJobConstants;
import com.github.kiu345.eclipse.eclipseai.jobs.SendConversationJob;
import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;
import com.github.kiu345.eclipse.eclipseai.model.ChatMessage.Type;
import com.github.kiu345.eclipse.eclipseai.model.Conversation;
import com.github.kiu345.eclipse.eclipseai.part.Attachment.FileContentAttachment;
import com.github.kiu345.eclipse.eclipseai.preferences.PreferenceConstants;
import com.github.kiu345.eclipse.eclipseai.prompt.ChatMessageFactory;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;
import com.github.kiu345.eclipse.eclipseai.services.ClientConfiguration;
import com.github.kiu345.eclipse.eclipseai.subscribers.AppendMessageToViewSubscriber;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class ChatPresenter {
    @Inject
    private ILog log;

    @Inject
    private PartAccessor partAccessor;

    @Inject
    private Conversation conversation;

    @Inject
    private ChatMessageFactory chatMessageFactory;

    @Inject
    private IJobManager jobManager;

    @Inject
    private Provider<SendConversationJob> sendConversationJobProvider;

    @Inject
    private AppendMessageToViewSubscriber appendMessageToViewSubscriber;

    @Inject
    private ApplyPatchWizardHelper applyPatchWizzardHelper;

    private ClientConfiguration configuration;

    private final List<Attachment> attachments = new ArrayList<>();

    @PostConstruct
    public void init() {
        appendMessageToViewSubscriber.setPresenter(this);
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propChangeListener);
    }

    public void onError(ChatMessage message) {
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setMessageHtml(message.getId(), message.getContent(), Type.ERROR);
        });
    }

    private ChatMessage createUserMessage(String userMessage) {
        ChatMessage message = chatMessageFactory.createUserChatMessage(() -> userMessage);
        message.setAttachments(attachments);
        return message;
    }

    public ChatMessage beginMessageFromAssistant() {
        ChatMessage message = chatMessageFactory.createAssistantChatMessage("");
        conversation.add(message);
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.appendMessage(message.getId(), message.getRole());
        });
        return message;
    }

    public void updateMessageFromAssistant(ChatMessage message) {
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setMessageHtml(message.getId(), message.getContent(), Type.MESSAGE);
        });
    }

    public void endMessageFromAssistant() {
        ChatMessage message = chatMessageFactory.createAssistantChatMessage("");
        conversation.add(message);
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.addInputBlock(message.getId());
        });
    }

    public ChatMessage beginMessageFromUI() {
        ChatMessage message = chatMessageFactory.createAssistantChatMessage("");
        conversation.add(message);
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.appendMessage(message.getId(), "user");
        });
        return message;
    }

    public void updateMessageFromUI(ChatMessage message) {
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setInputHtml(message.getId(), message.getContent());
        });
    }

    public ChatMessage insertInputMessageBlock() {
        ChatMessage message = chatMessageFactory.createAssistantChatMessage("");
        conversation.add(message);
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.insertInputMessageBlock(message.getId(), "user");
        });
        return message;
    }

    public void onClear() {
        onStop();
        if (configuration != null) {
            log.warn("configuration is null");
        }
        conversation.clear();
        attachments.clear();
        partAccessor.findMessageView().ifPresent(view -> {
            view.clearChatView();
        });
    }

    public void onSendPredefinedMessage(String text) {
        EclipseAIHandlerInvoker.Invoke(text);
    }

    public void onSendUserMessage(String text) {
        log.info("Send user message");
        ChatMessage message = createUserMessage(text);
        conversation.add(message);
        sendConversationJobProvider.get().schedule();
    }

    /**
     * Cancels all running AI query jobs
     */
    public void onStop() {
        var jobs = jobManager.find(null);
        Arrays.stream(jobs)
                .filter(job -> job.getName().startsWith(EclipseAIJobConstants.JOB_PREFIX))
                .forEach(Job::cancel);

        partAccessor.findMessageView().ifPresent(messageView -> {
//            messageView.setInputEnabled(true);
        });
    }

    /**
     * Copies the given code block to the system clipboard.
     *
     * @param codeBlock The code block to be copied to the clipboard.
     */
    public void onCopyCode(String codeBlock) {
        var clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
        var textTransfer = TextTransfer.getInstance();
        clipboard.setContents(new Object[] { codeBlock }, new Transfer[] { textTransfer });
        clipboard.dispose();
    }

    public void onApplyPatch(String codeBlock) {
        log.info("codeBlock = " + codeBlock);
        applyPatchWizzardHelper.showApplyPatchWizardDialog(codeBlock, null);
    }

    public void onSendPredefinedPrompt(Prompts type, ChatMessage message) {
        conversation.add(message);

        // update view
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.appendMessage(message.getId(), message.getRole());
            messageView.setMessageHtml(message.getId(), type.getDescription(), Type.MESSAGE);
        });

        // schedule message
        sendConversationJobProvider.get().schedule();
    }
/*
    public void onAddAttachment() {
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(() -> {
            FileDialog fileDialog = new FileDialog(display.getActiveShell(), SWT.OPEN);
            fileDialog.setText("Select an Image");

            // Retrieve the last selected directory from the preferences
            String lastSelectedDirectory = preferences.get(LAST_SELECTED_DIR_KEY, System.getProperty("user.home"));
            fileDialog.setFilterPath(lastSelectedDirectory);

            fileDialog.setFilterExtensions(new String[] { "*.png", "*.jpeg", "*.jpg" });
            fileDialog.setFilterNames(new String[] { "PNG files (*.png)", "JPEG files (*.jpeg, *.jpg)" });

            String selectedFilePath = fileDialog.open();

            if (selectedFilePath != null) {
                // Save the last selected directory back to the preferences
                String newLastSelectedDirectory = new File(selectedFilePath).getParent();
                preferences.put(LAST_SELECTED_DIR_KEY, newLastSelectedDirectory);

                try {
                    // Ensure that the preference changes are persisted
                    preferences.flush();
                }
                catch (BackingStoreException e) {
                    log.error("Error saving last selected directory preference", e);
                }

                ImageData[] imageDataArray = new ImageLoader().load(selectedFilePath);
                if (imageDataArray.length > 0) {
                    attachments.add(new Attachment.ImageAttachment(imageDataArray[0], createPreview(imageDataArray[0])));
                    applyToView(messageView -> {
                        messageView.setAttachments(attachments);
                    });
                }
            }
        });
    }
*/
    public void applyToView(Consumer<? super ChatViewPart> consumer) {
        partAccessor.findMessageView().ifPresent(consumer);
    }

    public void onImageSelected(Image image) {
        log.info("selected");
    }

    public void onAttachmentAdded(ImageData imageData) {
        attachments.add(new Attachment.ImageAttachment(imageData, createPreview(imageData)));
        applyToView(messageView -> {
//            messageView.setAttachments(attachments);
        });
    }

    public void onAttachmentAdded(FileContentAttachment attachment) {
        attachments.add(attachment);
        applyToView(messageView -> {
//            messageView.setAttachments(attachments);
        });
    }

    private IPropertyChangeListener propChangeListener = e -> {
        if (PreferenceConstants.ECLIPSEAI_PROVIDER.equals(e.getProperty()) ||
                PreferenceConstants.ECLIPSEAI_BASE_URL.equals(e.getProperty()) ||
                PreferenceConstants.ECLIPSEAI_API_BASE_PATH.equals(e.getProperty()) ||
                PreferenceConstants.ECLIPSEAI_GET_MODEL_API_PATH.equals(e.getProperty()) ||
                PreferenceConstants.ECLIPSEAI_API_KEY.equals(e.getProperty())) {
            partAccessor.findMessageView().ifPresent(view -> {
                view.makeComboList();
            });
        }
    };

}
