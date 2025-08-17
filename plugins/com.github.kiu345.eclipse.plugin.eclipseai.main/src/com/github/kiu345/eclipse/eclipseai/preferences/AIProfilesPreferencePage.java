package com.github.kiu345.eclipse.eclipseai.preferences;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
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
import org.osgi.service.prefs.BackingStoreException;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.ui.Messages;

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
    private Spinner spKeepAlive;
    private Label lblDescription;
    private Group detailGroup;

    public AIProfilesPreferencePage() {
        setDescription(Messages.preferencePage_description);
        Activator activator = Activator.getDefault();
        log = ILog.of(activator.getBundle());
    }

    @Override
    public void init(IWorkbench workbench) {
        profiles = ProfileStorage.loadProfiles();
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
        newProfile.setProvider(AiProvider.OLLAMA);
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
        log.info("Validate clicked");
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
        for (AiProvider p : AiProvider.values()) {
            comboProvider.add(p.getDisplayName());
        }
        comboProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        lblDescription = new Label(detailGroup, SWT.WRAP);
        GridData gdDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gdDesc.widthHint = 300;
        lblDescription.setLayoutData(gdDesc);

        comboProvider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = comboProvider.getSelectionIndex();
                if (idx >= 0) {
                    AiProvider provider = AiProvider.values()[idx];
                    lblDescription.setText(provider.getDescription());
                    updateDetailFieldsForProvider(provider);
                    detailGroup.layout(true, true);
                }
            }
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_urlBase);
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
                AiProvider provider = AiProvider.values()[comboProvider.getSelectionIndex()];
                switch (provider) {
                    case OPENAI -> selectedProfile.setOrganization(txtProviderSpecific.getText());
                    case GITHUB_COPILOT -> selectedProfile.setEndpoint(txtProviderSpecific.getText());
                    case OLLAMA -> selectedProfile.setModelApiPath(txtProviderSpecific.getText());
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

        new Label(detailGroup, SWT.NONE).setText(Messages.label_keepAlive);
        spKeepAlive = new Spinner(detailGroup, SWT.BORDER);
        spKeepAlive.setMaximum(999999);
        spKeepAlive.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setKeepAlive(spKeepAlive.getSelection());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_connectTimeout);
        spConnectTimeout = new Spinner(detailGroup, SWT.BORDER);
        spConnectTimeout.setMaximum(999999);
        spConnectTimeout.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setConnectTimeout(spConnectTimeout.getSelection());
        });

        new Label(detailGroup, SWT.NONE).setText(Messages.label_requestTimeout);
        spRequestTimeout = new Spinner(detailGroup, SWT.BORDER);
        spRequestTimeout.setMaximum(999999);
        spRequestTimeout.addModifyListener(e -> {
            if (selectedProfile != null)
                selectedProfile.setRequestTimeout(spRequestTimeout.getSelection());
        });

        if (selectedProfile == null) {
            setDetailEnabled(false);
        }
    }

    // --- Methode zum Aktivieren/Deaktivieren je Provider ---
    private void updateDetailFieldsForProvider(AiProvider provider) {
        txtUrlBase.setEnabled(provider != null); // für alle Provider
        txtApiPath.setEnabled(provider == AiProvider.OLLAMA);

        // Provider-spezifisches Feld
        switch (provider) {
            case OPENAI -> {
                lblProviderSpecific.setText(Messages.label_organization);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getOrganization() : ""));
            }
            case GITHUB_COPILOT -> {
                lblProviderSpecific.setText(Messages.label_endpoint);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getEndpoint() : ""));
            }
            case OLLAMA -> {
                lblProviderSpecific.setText(Messages.label_pathModels);
                txtProviderSpecific.setEnabled(true);
                txtProviderSpecific.setText(defaultString(selectedProfile != null ? selectedProfile.getModelApiPath() : ""));
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
        spKeepAlive.setEnabled(true);
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
        lblDescription.setText(profile.getProvider().getDescription());

        txtUrlBase.setText(profile.getUrlBase() != null ? profile.getUrlBase() : "");
        txtApiPath.setText(profile.getApiPath() != null ? profile.getApiPath() : "");
        txtApiKey.setText(profile.getApiKey() != null ? profile.getApiKey() : "");

        spConnectTimeout.setSelection(profile.getConnectTimeout());
        spRequestTimeout.setSelection(profile.getRequestTimeout());
        spKeepAlive.setSelection(profile.getKeepAlive());

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

        txtUrlBase.setText("");
        txtApiPath.setText("");
        txtApiKey.setText("");

        spConnectTimeout.setSelection(0);
        spRequestTimeout.setSelection(0);
        spKeepAlive.setSelection(0);

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
        ProfileStorage.saveProfiles(profiles); // speichert die Profile
        tableViewer.setInput(profiles); // Input neu setzen
        tableViewer.refresh(); // Tabelle aktualisieren
    }

    @Override
    protected void performDefaults() {
        profiles.clear();
        tableViewer.refresh();
        clearDetail();
    }
}
