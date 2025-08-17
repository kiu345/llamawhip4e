package com.github.kiu345.eclipse.eclipseai.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.ui.ChatPresenter.Settings;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 * Main chat view with chat tabs
 */
public class AIChatViewPart extends ViewPart {

    private class TabBarEventListener extends CTabFolder2Adapter implements SelectionListener {
        @Override
        public void close(CTabFolderEvent event) {
            Widget removedWidget = event.item;
            if (removedWidget instanceof CTabItem tabItem && tabItem.getControl() instanceof ChatComposite chatWidget) {
                removeChatTab(chatWidget);
            }
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            int idx = tabFolder.getSelectionIndex();
            activateTab(idx);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private static class TabSettings {
        private String modelName;
        private String chat;
        private Boolean useThink = true;
        private Boolean useTools = true;
        private Boolean useWeb = false;
        private Integer temp = 1;

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getChat() {
            return chat;
        }

        public void setChat(String chat) {
            this.chat = chat;
        }

        public Boolean isUseThink() {
            return useThink;
        }

        public void setUseThink(Boolean useThink) {
            this.useThink = useThink;
        }

        public Boolean isUseTools() {
            return useTools;
        }

        public void setUseTools(Boolean useTools) {
            this.useTools = useTools;
        }

        public Boolean isUseWeb() {
            return useWeb;
        }

        public void setUseWeb(Boolean useWeb) {
            this.useWeb = useWeb;
        }

        public Integer getTemp() {
            return temp;
        }

        public void setTemp(Integer temp) {
            this.temp = temp;
        }

    }

    private static class SettingStorage {
        private List<TabSettings> tabSettings = new ArrayList<>();;

        public List<TabSettings> getTabSettings() {
            return tabSettings;
        }

        @SuppressWarnings("unused")
        public void setTabSettings(List<TabSettings> tabSettings) {
            this.tabSettings = tabSettings;
        }

    }

    public static final String ID = "com.github.kiu345.eclipse.eclipseai.ui.AIChatViewPart";
    public static final String STORAGE_KEY = "eclipseai.ui.widgetstate";

    private final AtomicInteger chatCounter = new AtomicInteger(1);

    private CTabFolder tabFolder;

    private Composite toolbarComposite;
    private StackLayout toolbarLayout;

    private Composite controlComposite;

    private final List<ChatComposite> chatComposites = new ArrayList<>();

    final private TabBarEventListener tabBarEventListener = new TabBarEventListener();

    private boolean initDone = false;

    @Inject
    private IEclipseContext context;

    @Inject
    private ILog log;

    private IDialogSettings settings = Activator.getDefault().getDialogSettings();

    @PostConstruct
    @Override
    public void createPartControl(Composite parent) {
        if (initDone) {
            // not sure if we need this, but right now it seems that this method is not called without @PostConstruct
            return;
        }
        initDone = true;
        parent.setLayout(new GridLayout(1, false));

        controlComposite = new Composite(parent, SWT.NONE);
        GridLayout ctrlLayout = new GridLayout(2, false);
        ctrlLayout.marginWidth = 0;
        ctrlLayout.marginHeight = 0;
        controlComposite.setLayout(ctrlLayout);
        controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.RIGHT);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ToolBar topRightBar = new ToolBar(tabFolder, SWT.FLAT | SWT.RIGHT | SWT.WRAP);
        topRightBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        addAddChatItem(topRightBar);

        tabFolder.setTopRight(topRightBar);
        tabFolder.addCTabFolder2Listener(tabBarEventListener);
        tabFolder.addSelectionListener(tabBarEventListener);

        toolbarLayout = new StackLayout();
        toolbarComposite = new Composite(parent, SWT.NONE);
        toolbarComposite.setLayout(toolbarLayout);
        toolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        initState();
    }

    @PostContextCreate
    public void initState() {
        if (settings != null && StringUtils.isNotBlank(settings.get(STORAGE_KEY))) {
            try {
                SettingStorage settingsJson = new ObjectMapper()
                        .readerFor(SettingStorage.class)
                        .readValue(settings.get(STORAGE_KEY));
                for (var tabSetting : settingsJson.getTabSettings()) {
                    var tab = addNewChatTab(false);
                    if (tabSetting.getModelName() != null) {
                        log.info("restoring selected model to %s".formatted(tabSetting.getModelName()));
                        tab.setSelectedModel(tabSetting.getModelName());
                    }
                    Settings settings = tab.getPresenter().getSettings();

                    if (tabSetting.getTemp() != null) {
                        settings.setTemperatur(tabSetting.getTemp());
                    }
                    if (tabSetting.isUseThink() != null) {
                        settings.setThinkingAllowed(tabSetting.isUseThink());
                    }
                    if (tabSetting.isUseTools() != null) {
                        settings.setToolsAllowed(tabSetting.isUseTools());
                    }
                    if (tabSetting.isUseWeb() != null) {
                        settings.setWebAllowed(tabSetting.isUseWeb());
                    }
                    tab.getPresenter().setSettings(settings);
                    try {
                        tab.getPresenter().getConversationManager().deserializeConversation(tabSetting.getChat());
                        tab.resetMessages(tab.getPresenter().getConversationManager().messages());
                        tab.addInputElement();
                    }
                    catch (ClassNotFoundException | IOException e) {
                        log.warn("restoring chat failed: " + e.getMessage());
                    }
                }
            }
            catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (tabFolder.getItemCount() == 0) {
            addNewChatTab(true);
        }
        tabFolder.setSelection(0);
    }

    @PreDestroy
    public void saveState() {
        SettingStorage settingsValue = new SettingStorage();
        for (var chat : chatComposites) {
            if (chat.getPresenter().getConversationManager().isEmpty()) {
                continue;
            }
            TabSettings tabSetting = new TabSettings();
            tabSetting.setModelName(chat.getSelectedModel());
            tabSetting.setTemp(chat.getPresenter().getSettings().getTemperatur());
            tabSetting.setUseThink(chat.getPresenter().getSettings().getThinkingAllowed());
            tabSetting.setUseTools(chat.getPresenter().getSettings().getToolsAllowed());
            tabSetting.setUseWeb(chat.getPresenter().getSettings().getWebAllowed());
            try {
                tabSetting.setChat(chat.getPresenter().getConversationManager().serializeConversation());
            }
            catch (IOException e) {
                log.warn("persisting chat failed: " + e.getMessage());
            }
            settingsValue.getTabSettings().add(tabSetting);
        }
        try {
            settings.put(STORAGE_KEY, new ObjectMapper().writeValueAsString(settingsValue));
        }
        catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addAddChatItem(ToolBar topRightBar) {
        ToolItem addItem = new ToolItem(topRightBar, SWT.PUSH);
        addItem.setText(Messages.chat_new);
        addItem.setToolTipText(Messages.chat_new_descr);
        Image addIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_ADD);
        addItem.setImage(addIcon);

        addItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewChatTab(true);
                tabFolder.setSelection(tabFolder.getItemCount() - 1);
            }
        });
    }

    @Override
    public void setFocus() {
        if (!chatComposites.isEmpty()) {
            int index = tabFolder.getSelectionIndex();
            if (index >= 0 && index < chatComposites.size()) {
                chatComposites.get(index).focus();
            }
        }
    }

    /** Create a new chat tab and give it a unique name. */
    private ChatComposite addNewChatTab(boolean addInput) {
        String title = "Chat " + chatCounter.getAndIncrement();
        System.out.println("AIChatViewPart.addNewChatTab(" + title + ")");
        ChatComposite chatComposite = new ChatComposite(tabFolder, title);

        ChatPresenter presenter = new ChatPresenter(chatComposite);
        ContextInjectionFactory.inject(presenter, context);
        var widgetContext = context.createChild();
        widgetContext.set(ChatPresenter.class, presenter);
        ContextInjectionFactory.inject(chatComposite, widgetContext);

        chatComposite.initializeChatView(addInput);

        chatComposites.add(chatComposite);

        Image chatIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);

        CTabItem item = new CTabItem(tabFolder, SWT.CLOSE);
        item.setText(title);
        item.setImage(chatIcon);
        item.setControl(chatComposite);
        return chatComposite;
    }

    private void removeChatTab(ChatComposite chatWidget) {
        chatWidget.dispose();
        chatComposites.remove(chatWidget);
    }

    /** Switch UI to the tab at {@code index}. */
    private void activateTab(int index) {
        ChatComposite active = chatComposites.get(index);
        toolbarLayout.topControl = active.getToolbar();
        toolbarComposite.layout();
        active.setFocus();
    }
}
