package com.github.kiu345.eclipse.eclipseai.ui;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.PlatformUI;

import com.github.kiu345.eclipse.eclipseai.messaging.AgentMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg.Source;
import com.github.kiu345.eclipse.eclipseai.messaging.ToolsMsg;
import com.github.kiu345.eclipse.eclipseai.messaging.UserMsg;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor.Features;
import com.github.kiu345.eclipse.eclipseai.prompt.MessageParser;
import com.github.kiu345.eclipse.eclipseai.ui.BrowserScripting.ScriptException;
import com.github.kiu345.eclipse.eclipseai.ui.ChatPresenter.Settings;
import com.github.kiu345.eclipse.eclipseai.ui.actions.CopyCodeFunction;
import com.github.kiu345.eclipse.eclipseai.ui.actions.SaveCodeFunction;
import com.github.kiu345.eclipse.eclipseai.ui.actions.SendPromptFunction;
import com.github.kiu345.eclipse.eclipseai.ui.dnd.DropManager;
import com.github.kiu345.eclipse.eclipseai.ui.util.ComboBoxIdSelectionListener;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * Single component
 */
public class ChatComposite extends Composite {

    public enum State {
        NEW,
        SENDING,
        RECEIVING,
        FOLLOWUP
    }

    @Inject
    private ILog log;

    @Inject
    private ChatPresenter presenter;

    @Inject
    private DropManager dropManager;

    private Browser browser;

    private Button frmAttachBtn;
    private Button frmStopBtn;
    private Button frmClearBtn;
    private Button frmResendBtn;
    private Button frmRemoveLast;

    private final CTabFolder tabFolder;
    private Composite buttonsTab;
    private Composite optionsTab;
    private Combo frmOptionSelect;
    private Combo frmModelSelect;
    private Scale frmTempSlider;
    private Button frmAllowThink;
    private Button frmAllowWeb;
    private Button frmAllowFunctions;
    private Button frmRefreshBtn;

    private List<ModelDescriptor> aiModels = Collections.emptyList();

    @Inject
    private UISynchronize uiSync;

    private String wantedModelName = null;

    public ChatComposite(Composite parent, String title) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));

        /* Create the actual chat UI */
        browser = new Browser(this, SWT.EDGE);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        browser.setText(BrowserScripting.buildBaseHtml(false));
        initializeFunctions();

        tabFolder = new CTabFolder(this, SWT.BORDER);
        createTab();
    }

    private void uiSaveRunAsync(Runnable run) {
        if (Display.getCurrent() != null) {
            run.run();
        }
        else {
            uiSync.asyncExec(run);
        }
    }

    @PostConstruct
    private void init() {
        bindActions();
        setButtonStates(State.NEW);

        Display.getDefault().asyncExec(() -> {
            presenter.reloadModels();
        });
    }

    public void addInputElement() {
        System.out.println("ChatComposite.addInputElement()");
        uiSaveRunAsync(() -> {
            try {
                BrowserScripting.addElementContent(browser, "content", BrowserScripting.INPUT_HTML);
                BrowserScripting.runScript(browser, """
                        addKeyCapture();
                        window.scrollTo(0, document.body.scrollHeight);
                        """);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        setButtonStates(State.FOLLOWUP);
    }

    public void removeInputElement() {
        uiSaveRunAsync(() -> {
            BrowserScripting.removeElementById(browser, "edit_area");
        });
    }

    private void bindActions() {
        dropManager.registerDropTarget(browser);
        dropManager.registerDropTarget(frmAttachBtn);

        frmAttachBtn.addListener(SWT.Selection, e -> presenter.doAttachFile());
        frmStopBtn.addListener(SWT.Selection, e -> presenter.doStop());
        frmClearBtn.addListener(SWT.Selection, e -> presenter.doClear());
        frmResendBtn.addListener(SWT.Selection, e -> presenter.doResend());
        frmRemoveLast.addListener(SWT.Selection, e -> presenter.doRemoveLast());

        frmRefreshBtn.addListener(SWT.Selection, e -> presenter.doRefresh());
    }

    private void createTab() {
        tabFolder.setLayout(new GridLayout(1, false));
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        buttonsTab = new Composite(tabFolder, SWT.NONE);
        buttonsTab.setLayout(new GridLayout(5, false));
        buttonsTab.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        optionsTab = new Composite(tabFolder, SWT.NONE);
        optionsTab.setLayout(new GridLayout(4, false));
        optionsTab.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        CTabItem btnItem = new CTabItem(tabFolder, SWT.NONE);
        btnItem.setText("Chat");
        btnItem.setControl(buttonsTab);

        CTabItem optItem = new CTabItem(tabFolder, SWT.NONE);
        optItem.setText("Optionen");
        optItem.setControl(optionsTab);

        createActionTab();
        createOptionsTab();
        tabFolder.setSelection(btnItem);
    }

    private void createActionTab() {
        frmAttachBtn = new Button(buttonsTab, SWT.PUSH);
        frmAttachBtn.setText(Messages.chat_attach);
        frmAttachBtn.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_ADD));

        frmStopBtn = new Button(buttonsTab, SWT.PUSH);
        frmStopBtn.setText(Messages.chat_stop);
        frmStopBtn.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_ELCL_STOP));

        frmResendBtn = new Button(buttonsTab, SWT.PUSH);
        frmResendBtn.setText(Messages.chat_resend);
        frmResendBtn.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_ELCL_SYNCED));

        frmRemoveLast = new Button(buttonsTab, SWT.PUSH);
        frmRemoveLast.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_TOOL_BACK));
        frmRemoveLast.setText(Messages.chat_removeLast);

        frmClearBtn = new Button(buttonsTab, SWT.PUSH);
        frmClearBtn.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_TOOL_DELETE));
        frmClearBtn.setText(Messages.chat_clear);

        var frmTestBtn = new Button(buttonsTab, SWT.PUSH);
        frmTestBtn.setText("Test");
        frmTestBtn.addListener(SWT.Selection, (e) -> {
//            System.out.println("ChatComposite#frmTestBtn");
            var mgr = presenter.getConversationManager();
//            System.out.println(mgr.messages());
//            System.out.println("###");
//            mgr.removeLast();
//            System.out.println(mgr.messages());
            resetMessages(mgr.messages());
//            setInputText(UUID.randomUUID(), "<b>hi</b>\\''soä\n\nöüß~^#\"<y^^ä^«»®");
//            BrowserScripting.setElementContent(browser, "content", BrowserScripting.INPUT_HTML);
            addInputElement();
            setInputText("hiho");
        });
    }

    private void createOptionsTab() {

        Composite comboArea = new Composite(optionsTab, SWT.NONE);
        comboArea.setLayout(new GridLayout(2, false));

        Label lblConfig = new Label(comboArea, SWT.NONE);
        lblConfig.setText(Messages.chat_config);

        frmOptionSelect = new Combo(comboArea, SWT.READ_ONLY);
        frmOptionSelect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        frmOptionSelect.setItems(new String[] { "MainConfig" });
        frmOptionSelect.select(0);
        frmOptionSelect.setEnabled(false);

        Label lblModel = new Label(comboArea, SWT.NONE);
        lblModel.setText(Messages.chat_model);

        frmModelSelect = new Combo(comboArea, SWT.READ_ONLY);
        frmModelSelect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        frmModelSelect.addSelectionListener(new ComboBoxIdSelectionListener(index -> onModelSelect(index)));

        Composite checkboxArea = new Composite(optionsTab, SWT.NONE);
        checkboxArea.setLayout(new FillLayout(SWT.VERTICAL));

        frmAllowThink = new Button(checkboxArea, SWT.CHECK);
        frmAllowThink.setText(Messages.chat_allowThinking);
        frmAllowThink.setSelection(true);
        frmAllowThink.addListener(SWT.Selection, (e) -> {
            presenter.getSettings().setThinkingAllowed(frmAllowThink.getSelection());
        });
        frmAllowFunctions = new Button(checkboxArea, SWT.CHECK);
        frmAllowFunctions.setText(Messages.chat_allowTools);
        frmAllowFunctions.setSelection(true);
        frmAllowFunctions.addListener(SWT.Selection, (e) -> {
            presenter.getSettings().setToolsAllowed(frmAllowFunctions.getSelection());
        });
        frmAllowWeb = new Button(checkboxArea, SWT.CHECK);
        frmAllowWeb.setText(Messages.chat_allowWeb);
        frmAllowWeb.addListener(SWT.Selection, (e) -> {
            presenter.getSettings().setWebAllowed(frmAllowWeb.getSelection());
        });

        Composite tempArea = new Composite(optionsTab, SWT.NONE);
        tempArea.setLayout(new GridLayout(2, false));

        Label tempLabel = new Label(tempArea, SWT.NONE);
        tempLabel.setText(Messages.chat_temperature);
        frmTempSlider = new Scale(tempArea, SWT.HORIZONTAL);
        frmTempSlider.setMinimum(0);
        frmTempSlider.setMaximum(10);
        frmTempSlider.setIncrement(1);
        frmTempSlider.setPageIncrement(1);
        frmTempSlider.setSelection(1);
        frmTempSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        frmTempSlider.addListener(SWT.Selection, (e) -> {
            presenter.getSettings().setTemperatur(frmTempSlider.getSelection());
        });

        frmRefreshBtn = new Button(optionsTab, SWT.PUSH);
        frmRefreshBtn.setText(Messages.chat_refresh);
    }

    public void setButtonStates(final State state) {
        uiSaveRunAsync(() -> {
            switch (state) {
                case NEW:
                    frmModelSelect.setEnabled(true);
                    frmRefreshBtn.setEnabled(true);

                    frmAttachBtn.setEnabled(true);
                    frmStopBtn.setEnabled(false);
                    frmResendBtn.setEnabled(false);
                    frmRemoveLast.setEnabled(false);
                    frmClearBtn.setEnabled(true);
                    break;
                case SENDING:
                case RECEIVING:
                    frmModelSelect.setEnabled(false);
                    frmRefreshBtn.setEnabled(false);

                    frmAttachBtn.setEnabled(false);
                    frmStopBtn.setEnabled(true);
                    frmResendBtn.setEnabled(false);
                    frmRemoveLast.setEnabled(false);
                    frmClearBtn.setEnabled(false);
                    break;
                case FOLLOWUP:
                    frmModelSelect.setEnabled(true);
                    frmRefreshBtn.setEnabled(true);

                    frmAttachBtn.setEnabled(true);
                    frmStopBtn.setEnabled(false);
                    frmResendBtn.setEnabled(true);
                    frmRemoveLast.setEnabled(true);
                    frmClearBtn.setEnabled(true);
                    break;
                default:
                    // fallback if new states are forgotten,
                    // or for some reason is called with null
                    frmModelSelect.setEnabled(true);
                    frmRefreshBtn.setEnabled(true);

                    frmAttachBtn.setEnabled(true);
                    frmStopBtn.setEnabled(true);
                    frmResendBtn.setEnabled(true);
                    frmRemoveLast.setEnabled(true);
                    frmClearBtn.setEnabled(true);
                    break;
            }
        });
    }

    private ModelDescriptor onModelSelect(int index) {
        if (index < 0 || index >= aiModels.size()) {
            log.warn("Invalid model index " + index);
            return null;
        }
        var modelDef = aiModels.get(index);
        setSelectedModel(Optional.ofNullable(modelDef));
        return modelDef;
    }

    public void setSelectedModel(String name) {
        log.info("selecting model " + name);
        Optional<ModelDescriptor> selectedModel = aiModels.stream().filter(e -> name.equals(e.name())).findAny();
        if (selectedModel.isPresent()) {
            setSelectedModel(selectedModel);
            wantedModelName = null;
        }
        else {
            log.info("model not found, marking it for load on model list refresh");
            wantedModelName = name;
        }
    }

    public void setSelectedModel(Optional<ModelDescriptor> selectedModel) {
        if (selectedModel.isPresent()) {
            wantedModelName = null;
            ModelDescriptor modelDef = selectedModel.get();
            log.info("selecting model " + modelDef.name());
            presenter.setSelectedModel(modelDef);
            frmAllowThink.setEnabled(modelDef.features().contains(Features.THINKING));
            frmAllowFunctions.setEnabled(modelDef.features().contains(Features.TOOLS));
            frmAllowWeb.setEnabled(modelDef.features().contains(Features.TOOLS));
        }
        else {
            log.warn("invalid model");
            presenter.setSelectedModel(null);
        }
    }

    public String getSelectedModel() {
        if (presenter.getSelectedModel() == null) {
            return null;
        }
        return presenter.getSelectedModel().model();
    }

    public String browserReadyState() {
        if (Display.getCurrent() != null) {
            return BrowserScripting.browserReadyState(browser);
        }
        else {
            return Display.getDefault().syncCall(() -> {
                return BrowserScripting.browserReadyState(browser);
            });
        }
    }

    public void initializeChatView(boolean addInput) {
        // Initialize the browser with base HTML and CSS
        uiSaveRunAsync(() -> {
            try {
                browser.setText(BrowserScripting.buildBaseHtml(addInput));
            }
            catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });
    }

    public void createElement(Msg message) {
        createElement(message.getMessageId(), message.getSource());
        updateElement(message);
    }

    public void updateElement(Msg message) {
        String messageText = message.getMessage();
        if (message instanceof AgentMsg agentMsg) {
            if (!StringUtils.isBlank(agentMsg.getThinking())) {
                StringBuilder msgBuilder = new StringBuilder();
                msgBuilder.append("<think>\n");
                msgBuilder.append(agentMsg.getThinking());
                msgBuilder.append("</think>\n");
                msgBuilder.append(agentMsg.getMessage());
                messageText = msgBuilder.toString();
            }

        }
        updateElement(message.getMessageId(), message.getType(), messageText, createFooter(message), true);
    }

    public String createFooter(Msg message) {
        if (message == null || message.getTimestamp() == null) {
            return null;
        }

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zdt = message.getTimestamp().atZone(zone);

        String dateString = message.getTimestamp() == null ? "-" : DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(zdt);
        return (message.getTimings() != null) ? "<span class=\"timestamp\">%.1fs, %s</span>".formatted(message.getTimings(), dateString)
                : "<span class=\"timestamp\">%s</span>".formatted(dateString);
    }

    public void createElement(UUID messageId, Source source) {
        uiSaveRunAsync(() -> {
            String msgHtml = BrowserScripting.messageHtml(messageId, source);
            try {
                BrowserScripting.addElementContent(browser, "content", msgHtml);
            }
            catch (ScriptException ex) {
                log.error("error executing script in create element: " + ex.getMessage());
            }

//            browser.execute("""
//                    node = document.createElement("div");
//                    node.setAttribute("id", "message-${id}");
//                    node.setAttribute("class", "${cssClass}");
//                    document.getElementById("content").appendChild(node);
//                        """.replace("${id}", messageId.toString()).replace("${cssClass}", cssClass));
            // Scroll down
//            browser.execute("window.scrollTo(0, document.body.scrollHeight);");
//            frmAttachBtn.setEnabled(false);
//            frmStopBtn.setEnabled(true);
        });
    }

    public void updateElement(UUID messageId, Msg.Type type, String body) {
        updateElement(messageId, type, body, null, false);
    }

    public void updateElement(UUID messageId, Msg.Type type, String body, String footerHtml, boolean scroll) {
        uiSaveRunAsync(() -> {
            MessageParser parser = new MessageParser();
            String fixedHtml = BrowserScripting.escapeHtmlQuotes(BrowserScripting.fixLineBreaks(parser.parseToHtml(messageId, body)));
            switch (type) {
                case ERROR:
                    fixedHtml = "<div style=\"background-color: #FFCCCC;\"><p><b>ERROR:</b></p>" + fixedHtml + "</div>";
                    break;
                default:
                    ;
            }
            if (StringUtils.isNotBlank(footerHtml)) {
                fixedHtml = fixedHtml.concat("</div><div class=\"footer\">%s</div>".formatted(footerHtml));
            }
            // inject and highlight html message
            browser.execute(
                    """
                            var element = document.getElementById("%s");
                            element.innerHTML = '%s';
                            hljs.highlightAll();"""
                            .formatted(BrowserScripting.MSG_ID_PREFIX + messageId.toString(), fixedHtml)
            );
            // Scroll down
            if (scroll) {
                browser.execute("hljs.highlightAll();\nwindow.scrollTo(0, document.body.scrollHeight);");
            }
            else {
                browser.execute(" hljs.highlightAll();");
            }
        });
    }

    public void setInputText(String message) {
        uiSaveRunAsync(() -> {
            var fixedMessage = StringEscapeUtils.escapeHtml4(message).replace("\n", "<br/>\n");
            BrowserScripting.setElementContent(browser, "inputarea", fixedMessage);

            // Scroll down and enable input handling
            browser.execute("addKeyCapture();");
            browser.execute("window.scrollTo(0, document.body.scrollHeight);");
        });
    }

    public void clearChatView(boolean addInput) {
        uiSaveRunAsync(() -> {
            frmResendBtn.setEnabled(false);
            initializeChatView(addInput);
        });
    }

    public void resetMessages(List<Msg> messages) {
        clearChatView(false);
        int stopLoop = 0;
        while (!"complete".equalsIgnoreCase(browserReadyState()) && stopLoop++ < 10) {
            Thread.yield();
        }

//        BrowserScripting.removeElementById(browser, "edit_area");
        for (Msg msg : messages) {
//            System.out.println("adding message" + msg);
            if (!(msg instanceof ToolsMsg)) {
                createElement(msg);
            }
        }
        BrowserScripting.removeElementById(browser, "edit_area");
    }

    private void initializeFunctions() {
        new SendPromptFunction(browser, "eclipseSendPrompt", this::prepareDoSend);
        new CopyCodeFunction(browser, "eclipseCopyCode", this::prepareDoCopyCode);
        new SaveCodeFunction(browser, "eclipseSaveCode", this::prepareDoSaveCode);
    }

    public void prepareDoSend(String userPrompt, boolean predefinedPrompt) {
        final UUID messageId = UUID.randomUUID();
        final UserMsg message = new UserMsg(messageId, userPrompt);
        message.setPredefinedPrompt(predefinedPrompt);
        uiSaveRunAsync(() -> {
            setButtonStates(State.SENDING);
            createElement(message);
        });
        presenter.doSend(messageId, userPrompt, predefinedPrompt);
    }

    public void prepareDoCopyCode(String codeBlock) {
        presenter.doCopyCode(codeBlock);
    }

    public void prepareDoSaveCode(String codeBlock, String fileName) {
        presenter.doSaveCode(codeBlock, fileName);
    }

    public void setModelList(final List<ModelDescriptor> aiModels) {
        this.aiModels = aiModels;
        final String[] models = aiModels.stream()
                .map(e -> e.name())
                .toArray(String[]::new);

        uiSaveRunAsync(() -> {
            frmModelSelect.setItems(models);
            if (wantedModelName != null) {
                log.info("trying to find " + wantedModelName);
                var modelSearch = aiModels.stream().filter(e -> wantedModelName.equals(e.name())).findAny();
                if (modelSearch.isPresent()) {
                    log.info("found");
                    var model = modelSearch.get();
                    int index = aiModels.indexOf(model);
                    frmModelSelect.select(index);
                    onModelSelect(index);
                }
            }
            if (frmModelSelect.getSelectionIndex() < 0 && models.length > 0) {
                log.info("resetting selection");
                frmModelSelect.select(0);
                onModelSelect(0);
            }
        });
    }

    public void focus() {
        browser.setFocus();
    }

    public Composite getToolbar() {
        return tabFolder;
    }

    public ChatPresenter getPresenter() {
        return presenter;
    }

    public void updateWith(final Settings settings) {
        uiSaveRunAsync(() -> {
            frmAllowFunctions.setSelection(settings.getToolsAllowed());
            frmAllowThink.setSelection(settings.getThinkingAllowed());
            frmAllowWeb.setSelection(settings.getWebAllowed());
            frmTempSlider.setSelection(settings.getTemperatur());
        });
    }
}
