package com.github.kiu345.eclipse.llamawhip.ui.pref;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.osgi.service.prefs.BackingStoreException;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.config.AIProfileStorage;
import com.github.kiu345.eclipse.llamawhip.config.AIProvider;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;
import com.github.kiu345.eclipse.llamawhip.ui.Messages;

/**
 * Preference page for managing AI provider profiles.
 */
public class AIProfilesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private ILog log;

    private TableViewer tableViewer;
    private List<AIProviderProfile> profiles = new ArrayList<>();
    private AIProviderProfile selectedProfile;

    private Text txtName;
    private Combo comboProvider;
    private Text txtUrlBase;
    private Label lblProviderSpecific;
    private Text txtProviderSpecific;
    private Text txtApiPath;
    private Text txtApiKey;
    private Spinner spConnectTimeout;
    private Spinner spRequestTimeout;
    private Text txtKeepAlive;
    private Hyperlink lblDescription;
    private Group detailGroup;

    private Label lblBaseData;

    public AIProfilesPreferencePage() {
        setTitle(Messages.preferencePage_models_title);
        setDescription(Messages.preferencePage_models_descr);
        Activator activator = Activator.getDefault();
        log = ILog.of(activator.getBundle());
    }

    @Override
    public void init(IWorkbench workbench) {
        profiles = AIProfileStorage.loadProfiles();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        createMasterPart(container);
        createDetailPart(container);

        if (selectedProfile == null) {
            setDetailEnabled(false);
        }
        return container;
    }

    private void createMasterPart(Composite parent) {
        Composite masterComposite = new Composite(parent, SWT.NONE);
        masterComposite.setLayout(new GridLayout(2, false));
        masterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tableViewer = new TableViewer(masterComposite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        createTableColumns();

        tableViewer.addSelectionChangedListener(e -> onProfileSelected());
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite btnComposite = new Composite(masterComposite, SWT.NONE);
        btnComposite.setLayout(new GridLayout(1, false));
        btnComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        createButton(btnComposite, Messages.button_add, this::onAddProfile);
        createButton(btnComposite, Messages.button_remove, this::onRemoveProfile);
        createButton(btnComposite, Messages.button_defaults, this::onProfileDefaults);
        createButton(btnComposite, Messages.button_validate, this::onValidateProfile);

        tableViewer.setInput(profiles);
    }

    private Button createButton(Composite parent, String text, Listener listener) {
        Button btn = new Button(parent, SWT.PUSH);
        btn.setText(text);
        btn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        btn.addListener(SWT.Selection, listener);
        return btn;
    }

    private void onAddProfile(Event event) {
        AIProviderProfile newProfile = new AIProviderProfile();
        newProfile.setName("New Profile");
        newProfile.setProvider(AIProvider.OLLAMA);
        profiles.add(newProfile);
        tableViewer.refresh();
        tableViewer.setSelection(new StructuredSelection(newProfile));
        selectedProfile = newProfile;
        loadProfileIntoDetail(newProfile);
        log.info("Profile added");
    }

    private void onRemoveProfile(Event event) {
        if (selectedProfile != null) {
            profiles.remove(selectedProfile);
            selectedProfile = null;
            tableViewer.refresh();
            clearDetail();
            setDetailEnabled(false);
            log.info("Profile removed");
        }
    }

    private void onProfileDefaults(Event event) {
        if (selectedProfile != null) {
            selectedProfile.defaults();
            loadProfileIntoDetail(selectedProfile);
        }
    }

    private void onValidateProfile(Event event) {
        if (selectedProfile != null) {
            try {
                String[] errors = ChatAdapterFactory.validate(selectedProfile);
                if (errors != null && errors.length > 0) {
                    log.warn("profile invalid");

                    MessageDialog.openError(
                            getShell(),
                            "Validation",
                            "Validation errors:\n".concat(String.join("\n", errors))
                    );
                }
                else {
                    MessageDialog.openInformation(
                            getShell(),
                            "Validation",
                            "Profile OK"
                    );
                }
            }
            catch (Exception ex) {
                log.warn(ex.getMessage(), ex);
            }
        }
    }

    private void onProfileSelected() {
        IStructuredSelection sel = tableViewer.getStructuredSelection();
        if (!sel.isEmpty()) {
            selectedProfile = (AIProviderProfile) sel.getFirstElement();
            loadProfileIntoDetail(selectedProfile);
        }
    }

    private void createTableColumns() {
        TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
        colName.getColumn().setText(Messages.label_name);
        colName.getColumn().setWidth(200);
        colName.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((AIProviderProfile) element).getName();
            }
        });

        TableViewerColumn colProvider = new TableViewerColumn(tableViewer, SWT.NONE);
        colProvider.getColumn().setText(Messages.label_provider);
        colProvider.getColumn().setWidth(150);
        colProvider.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((AIProviderProfile) element).getProvider().toString();
            }
        });

        TableViewerColumn colUrl = new TableViewerColumn(tableViewer, SWT.NONE);
        colUrl.getColumn().setText(Messages.label_urlBase);
        colUrl.getColumn().setWidth(250);
        colUrl.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((AIProviderProfile) element).getUrlBase();
            }
        });
    }

    private void createDetailPart(Composite parent) {
        // DETAIL-Teil
        detailGroup = new Group(parent, SWT.NONE);
        detailGroup.setText("Profile Details");
        GridLayout layout = new GridLayout(6, false);
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 5;
        detailGroup.setLayout(layout);
        detailGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        // --- Zeile 1: Name + Provider + Beschreibung ---
        new Label(detailGroup, SWT.NONE).setText(Messages.label_name);
        txtName = new Text(detailGroup, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtName.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setName(txtName.getText());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_provider);
        comboProvider = new Combo(detailGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (AIProvider p : AIProvider.values()) {
            comboProvider.add(p.getDisplayName());
        }
        comboProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        lblDescription = new Hyperlink(detailGroup, SWT.WRAP);
        lblDescription.setText("");
        lblDescription.setHref("");
        GridData gdDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gdDesc.widthHint = 300;
        lblDescription.setLayoutData(gdDesc);

        comboProvider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = comboProvider.getSelectionIndex();
                if (idx >= 0) {
                    AIProvider provider = AIProvider.values()[idx];
                    updateDetailFieldsForProvider(provider);
                    detailGroup.layout(true, true);
                }
            }
        });

        lblBaseData = new Label(detailGroup, SWT.NONE);
        lblBaseData.setText(Messages.label_urlBase);
        txtUrlBase = new Text(detailGroup, SWT.BORDER);
        txtUrlBase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        txtUrlBase.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setUrlBase(txtUrlBase.getText());
        });

        lblProviderSpecific = new Label(detailGroup, SWT.NONE);
        txtProviderSpecific = new Text(detailGroup, SWT.BORDER);
        txtProviderSpecific.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        txtProviderSpecific.addModifyListener(e -> {
            if (selectedProfile != null) {
                if (comboProvider.getSelectionIndex() < 0) {
                    return;
                }
                AIProvider provider = AIProvider.values()[comboProvider.getSelectionIndex()];
                selectedProfile.setProvider(provider);
                switch (provider) {
                    case OPENAI -> selectedProfile.setOrganization(txtProviderSpecific.getText());
//                    case GITHUB_COPILOT -> selectedProfile.setEndpoint(txtProviderSpecific.getText());
                    case OLLAMA -> selectedProfile.setModelPath(txtProviderSpecific.getText());
                    case JLAMA -> selectedProfile.setModelPath(txtProviderSpecific.getText());
                    case LOCALAI -> selectedProfile.setModelPath(txtProviderSpecific.getText());
                    default -> throw new IllegalArgumentException("Unexpected value: " + provider);
                }
            }
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_pathApi);
        txtApiPath = new Text(detailGroup, SWT.BORDER);
        txtApiPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtApiPath.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setApiPath(txtApiPath.getText());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_apiKey);
        txtApiKey = new Text(detailGroup, SWT.BORDER);
        txtApiKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtApiKey.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setApiKey(txtApiKey.getText());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_connectTimeout);
        spConnectTimeout = new Spinner(detailGroup, SWT.BORDER);
        spConnectTimeout.setMaximum(3600);
        spConnectTimeout.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setConnectTimeout(spConnectTimeout.getSelection());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_requestTimeout);
        spRequestTimeout = new Spinner(detailGroup, SWT.BORDER);
        spRequestTimeout.setMaximum(3600);
        spRequestTimeout.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setRequestTimeout(spRequestTimeout.getSelection());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_keepAlive);
        txtKeepAlive = new Text(detailGroup, SWT.BORDER);
        txtKeepAlive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtKeepAlive.setTextLimit(4);
        txtKeepAlive.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                if (e.keyCode == KeyEvent.VK_BACK_SPACE || e.keyCode == KeyEvent.VK_DELETE) {
                    e.doit = true;
                }
                else if (!Character.isDigit(e.character)) {
                    e.doit = false;
                }

            }
        });
        txtKeepAlive.addModifyListener(e -> {
            String input = txtKeepAlive.getText();
            if (StringUtils.isBlank(input)) {
                txtKeepAlive.setText("");
                input = null;
            }
            if (selectedProfile != null) {
                try {
                    Integer value = Integer.getInteger(input);
                    if (value != null) {
                        if (value > 3600) {
                            value = 3600;
                        }
                        if (value < 0) {
                            value = 0;
                        }
                    }
                    selectedProfile.setKeepAlive(value);
                    txtKeepAlive.setText(intToString(value));
                }
                catch (NumberFormatException ex) {
                    txtKeepAlive.setText("");
                }
            }
        });
        if (selectedProfile == null) {
            setDetailEnabled(false);
        }
    }

    // --- Methode zum Aktivieren/Deaktivieren je Provider ---
    private void updateDetailFieldsForProvider(AIProvider provider) {
        txtUrlBase.setEnabled(provider != null); // für alle Provider
        txtApiPath.setEnabled(provider == AIProvider.OLLAMA);
        lblDescription.setText("" + provider.getDescription());
        lblDescription.setHref("" + provider.getUrl());
        lblBaseData.setText(Messages.label_urlBase);

        // Provider-spezifisches Feld
        switch (provider) {
            case OPENAI -> {
                lblProviderSpecific.setText(Messages.label_organization);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getOrganization() : ""));
            }
//            case GITHUB_COPILOT -> {
//                lblProviderSpecific.setText(Messages.label_endpoint);
//                txtProviderSpecific.setEnabled(true);
//                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getEndpoint() : ""));
//            }
            case OLLAMA -> {
                lblProviderSpecific.setText(Messages.label_pathModels);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getModelPath() : ""));
            }
            case JLAMA -> {
                lblBaseData.setText("Modelnames");
                lblProviderSpecific.setText("Cachepath");
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getModelPath() : ""));
            }
            case LOCALAI -> {
                lblProviderSpecific.setText(Messages.label_pathModels);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getModelPath() : ""));
            }
            default -> {
                lblProviderSpecific.setText("");
                txtProviderSpecific.setEnabled(false);
                txtProviderSpecific.setText("");
            }
        }

        txtApiKey.setEnabled(true);

        spConnectTimeout.setEnabled(true);
        spRequestTimeout.setEnabled(true);
        txtKeepAlive.setEnabled(true);
    }

    private void loadProfileIntoDetail(AIProviderProfile profile) {
        if (profile == null) {
            clearDetail();
            setDetailEnabled(false);
            updateDetailFieldsForProvider(null);
            return;
        }

        setDetailEnabled(true);

        txtName.setText(profile.getName() != null ? profile.getName() : "");
        comboProvider.select(profile.getProvider().ordinal());

        txtUrlBase.setText(profile.getUrlBase() != null ? profile.getUrlBase() : "");
        txtApiPath.setText(profile.getApiPath() != null ? profile.getApiPath() : "");
        txtApiKey.setText(profile.getApiKey() != null ? profile.getApiKey() : "");

        spConnectTimeout.setSelection(profile.getConnectTimeout());
        spRequestTimeout.setSelection(profile.getRequestTimeout());
        txtKeepAlive.setText(intToString(profile.getKeepAlive()));

        updateDetailFieldsForProvider(profile.getProvider());

        detailGroup.layout(true, true);
    }

    private void setDetailEnabled(boolean enabled) {
        detailGroup.setEnabled(enabled);
        for (Control child : detailGroup.getChildren()) {
            child.setEnabled(enabled);
        }
    }

    private void clearDetail() {
        txtName.setText("");
        comboProvider.deselectAll();
        lblDescription.setText("");
        lblDescription.setHref("");

        txtUrlBase.setText("");
        txtApiPath.setText("");
        txtApiKey.setText("");

        spConnectTimeout.setSelection(0);
        spRequestTimeout.setSelection(0);
        txtKeepAlive.setText("");

        // Provider-spezifisches Feld
        lblProviderSpecific.setText("");
        txtProviderSpecific.setText("");
        txtProviderSpecific.setEnabled(false);

        detailGroup.layout(true, true); // Layout aktualisieren
    }

    @Override
    public boolean performOk() {
        try {
            saveProfile();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void performApply() {
        try {
            saveProfile();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProfile() throws IOException, BackingStoreException {
        AIProfileStorage.saveProfiles(profiles); // speichert die Profile
        tableViewer.setInput(profiles); // Input neu setzen
        tableViewer.refresh(); // Tabelle aktualisieren
    }

    @Override
    protected void performDefaults() {
        profiles.clear();
        tableViewer.refresh();
        clearDetail();
    }

    private String intToString(Integer input) {
        return input != null ? Integer.toString(input) : "";
    }
}
