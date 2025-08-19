package com.github.kiu345.eclipse.llamawhip.ui.pref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.config.AIProfileStorage;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.ui.Messages;

public class EclipseAIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private class PreferenceListener implements org.eclipse.jface.util.IPropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
        }

    }

    private static final String PREF_ENTRIES = "keyReplacement.entries";
    private static final String PREF_LANGUAGE = "general.language";
    private static final String PREF_COMPLETION = "codecompletion.enabled";
    private static final String PREF_LIMIT_CONTEXT = "codecompletion.limitcontext";
    private static final String PREF_CONTEXT_BEFORE = "codecompletion.contextbefore";
    private static final String PREF_CONTEXT_AFTER = "codecompletion.contextafter";

    private static final String FIELD_SEP = "\u001F";
    private static final String RECORD_SEP = "\u001E";
    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z]+");

    private TableViewer viewer;
    private final List<Entry> entries = new ArrayList<>();
    private Combo languageCombo;
    private Combo defaultProviderCombo;
    private Combo defaultModelCombo;
    private Combo ccModelCombo;
    private Spinner ccTimeout;
    private Button ccAllowTools;
    private Button completionCheckbox;
    private Button limitContextCheckbox;
    private Spinner contextBeforeSpinner;
    private Spinner contextAfterSpinner;

    private List<AIProviderProfile> profiles = Collections.emptyList();
    private List<ModelDescriptor> models = Collections.emptyList();

    private final PluginConfiguration config = PluginConfiguration.instance();
    private final PreferenceListener preferenceListener = new PreferenceListener();
    private ILog log;
    private Button ccAllowThink;

    @Override
    public void init(IWorkbench workbench) {
        setTitle(Messages.preferencePage_main_title);
        setDescription(Messages.preferencePage_main_descr);
        setPreferenceStore(config.store());
        log = Platform.getLog(getClass());
        config.store().addPropertyChangeListener(preferenceListener);
    }

    @Override
    public void dispose() {
        if (log == null) {
            throw new IllegalStateException("component already cleaned up");
        }
        config.store().removePropertyChangeListener(preferenceListener);
        log = null;
        super.dispose();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(1).margins(8, 8).applyTo(root);

        // General group
        Group generalGroup = new Group(root, SWT.NONE);
        generalGroup.setText("General");
        GridLayoutFactory.swtDefaults().numColumns(2).margins(8, 8).applyTo(generalGroup);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(generalGroup);

        new Label(generalGroup, SWT.NONE).setText("Language:");
        languageCombo = new Combo(generalGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        languageCombo.setItems(new String[] { "Default", "Deutsch", "Englisch" });
        languageCombo.select(0);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(languageCombo);

        new Label(generalGroup, SWT.NONE).setText("Default provider:");
        defaultProviderCombo = new Combo(generalGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultProviderCombo);
        defaultProviderCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onProviderSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                onProviderSelected(null);
            }
        });

        new Label(generalGroup, SWT.NONE).setText("Default model:");
        defaultModelCombo = new Combo(generalGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultModelCombo);

        // Code Completion group
        Group completionGroup = new Group(root, SWT.NONE);
        completionGroup.setText("Code Assistant");
        GridLayoutFactory.swtDefaults().numColumns(2).margins(8, 8).applyTo(completionGroup);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(completionGroup);

        completionCheckbox = new Button(completionGroup, SWT.CHECK);
        completionCheckbox.setText("Enable code completion");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(completionCheckbox);

        new Label(completionGroup, SWT.NONE).setText("Timeout:");
        ccTimeout = new Spinner(completionGroup, SWT.BORDER);
        ccTimeout.setMinimum(0);
        ccTimeout.setMaximum(180);
        ccTimeout.setSelection(30);

        ccAllowThink = new Button(completionGroup, SWT.CHECK);
        ccAllowThink.setText(Messages.chat_allowThinking);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(ccAllowThink);

        ccAllowTools = new Button(completionGroup, SWT.CHECK);
        ccAllowTools.setText(Messages.chat_allowTools);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(ccAllowTools);

        limitContextCheckbox = new Button(completionGroup, SWT.CHECK);
        limitContextCheckbox.setText("Limit context");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(limitContextCheckbox);

        new Label(completionGroup, SWT.NONE).setText("Context before:");
        contextBeforeSpinner = new Spinner(completionGroup, SWT.BORDER);
        contextBeforeSpinner.setMinimum(0);
        contextBeforeSpinner.setMaximum(1000);
        contextBeforeSpinner.setSelection(0);

        new Label(completionGroup, SWT.NONE).setText("Context after:");
        contextAfterSpinner = new Spinner(completionGroup, SWT.BORDER);
        contextAfterSpinner.setMinimum(0);
        contextAfterSpinner.setMaximum(1000);
        contextAfterSpinner.setSelection(0);

        // enable/disable spinners based on checkbox
        SelectionAdapter toggleContext = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = limitContextCheckbox.getSelection();
                contextBeforeSpinner.setEnabled(enabled);
                contextAfterSpinner.setEnabled(enabled);
            }
        };
        limitContextCheckbox.addSelectionListener(toggleContext);
        toggleContext.widgetSelected(null); // initialize

        new Label(completionGroup, SWT.NONE).setText("Model:");
        ccModelCombo = new Combo(completionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(ccModelCombo);

        // Prompt Variables table group
        Group tableGroup = new Group(root, SWT.NONE);
        tableGroup.setText("Prompt Variablen");
        GridLayoutFactory.swtDefaults().numColumns(2).margins(8, 8).applyTo(tableGroup);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableGroup);

        viewer = new TableViewer(tableGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setEnabled(false);
        GridDataFactory.fillDefaults().grab(true, true).hint(600, 300).applyTo(table);

        viewer.setContentProvider(ArrayContentProvider.getInstance());

        // Key column
        TableViewerColumn keyCol = new TableViewerColumn(viewer, SWT.LEFT);
        TableColumn keyColumn = keyCol.getColumn();
        keyColumn.setText("Key");
        keyColumn.setWidth(200);
        keyCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Entry) element).key;
            }
        });
        keyCol.setEditingSupport(new KeyEditingSupport(viewer));

        // Replacement Text column
        TableViewerColumn textCol = new TableViewerColumn(viewer, SWT.LEFT);
        TableColumn textColumn = textCol.getColumn();
        textColumn.setText("Replacement Text");
        textColumn.setWidth(380);
        textCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Entry) element).text;
            }
        });
        textCol.setEditingSupport(new TextEditingSupport(viewer));

        // Buttons
        Composite buttons = new Composite(tableGroup, SWT.NONE);
        GridLayoutFactory.fillDefaults().spacing(0, 6).applyTo(buttons);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttons);

        Button add = new Button(buttons, SWT.PUSH);
        add.setText("Hinzufügen");
        add.setEnabled(false);
        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        add.addListener(SWT.Selection, e -> {
            Entry newEntry = new Entry("", "");
            entries.add(newEntry);
            viewer.refresh();
            viewer.editElement(newEntry, 0);
        });

        Button remove = new Button(buttons, SWT.PUSH);
        remove.setText("Entfernen");
        remove.setEnabled(false);
        remove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        remove.addListener(SWT.Selection, e -> {
            Entry sel = getSelection();
            if (sel != null) {
                entries.remove(sel);
                viewer.refresh();
            }
        });

        Button up = new Button(buttons, SWT.PUSH);
        up.setText("Hoch");
        up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        up.addListener(SWT.Selection, e -> move(-1));

        Button down = new Button(buttons, SWT.PUSH);
        down.setText("Runter");
        down.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        down.addListener(SWT.Selection, e -> move(1));

        viewer.setInput(entries);
        loadProfiles();
        loadFromPrefs();

        return root;
    }

    private void loadProfiles() {
        profiles = AIProfileStorage.loadProfiles();
        int selected = defaultProviderCombo.getSelectionIndex();
        String[] profileNames = profiles.stream().map(e -> e.getProvider() + ":" + e.getName()).toArray(String[]::new);
        defaultProviderCombo.setItems(profileNames);
        if (profileNames.length == 0) {
            setErrorMessage("Define a provider profile first");
        }
        else {
            setErrorMessage(null);
        }
        defaultProviderCombo.select(selected);
    }

    @Override
    public void setVisible(boolean visible) {
        try {
            // we need this because init is only called once. otherwise
            // we don't see new profiles or have dead entities in the list
            if (visible == true) {
                loadProfiles();
            }
        }
        catch (Exception ex) {
            if (log != null) {
                log.error(ex.getMessage());
            }
        }
        super.setVisible(visible);
    }

    private void onProviderSelected(SelectionEvent e) {
        if (e == null || defaultProviderCombo.getSelectionIndex() < 0) {
            defaultModelCombo.clearSelection();
            ccModelCombo.clearSelection();
            return;
        }
        reloadComboItems(false);
    }

    private CompletableFuture<List<ModelDescriptor>> reloadComboItems(boolean loadPerfs) {
        int selectedIndex = defaultProviderCombo.getSelectionIndex();
        return CompletableFuture.supplyAsync(() -> {
            log.info("loading models");
            return models = loadModels(selectedIndex);
        }).thenApply((models) -> {
            Display.getDefault().asyncExec(() -> {
                if (loadPerfs) {
                    defaultModelCombo.setEnabled(false);
                    ccModelCombo.setEnabled(false);
                }
                setComboBoxItems(defaultModelCombo, models);
                setComboBoxItems(ccModelCombo, models);

                if (loadPerfs) {
                    if (StringUtils.isNotBlank(config.getDefaultModel())) {
                        setComboBoxSelect(defaultModelCombo, config.getDefaultModel());
                    }
                    else {
                        defaultModelCombo.clearSelection();
                    }

                    if (StringUtils.isNotBlank(config.getCcModel())) {
                        setComboBoxSelect(ccModelCombo, config.getCcModel());
                    }
                    else {
                        ccModelCombo.clearSelection();
                    }
                    defaultModelCombo.setEnabled(true);
                    ccModelCombo.setEnabled(true);
                }
            });
            return models;
        }).whenComplete((models, ex) -> {
            if (ex != null) {
                log.error(ex.getMessage(), ex);
            }
            else {
            }
        });

    }

    private List<ModelDescriptor> loadModels(int providerIndex) {
        var provider = profiles.get(providerIndex);
        var adapter = ChatAdapterFactory.create(log, provider);
        return adapter.getModels();

    }

    private void setComboBoxItems(Combo target, List<ModelDescriptor> items) {
        String[] newItems = items.stream()
                .map(ModelDescriptor::name)
                .toArray(String[]::new);

        String oldSelected = null;
        int oldIndex = target.getSelectionIndex();
        if (oldIndex >= 0) {
            oldSelected = target.getItem(oldIndex);
        }
        target.setItems(newItems);

        if (oldSelected != null) {
            int newIndex = -1;
            for (int i = 0; i < newItems.length; i++) {
                if (oldSelected.equals(newItems[i])) {
                    newIndex = i;
                    break;
                }
            }
            if (newIndex >= 0) {
                target.select(newIndex);
            }
            else {
                target.clearSelection();
            }
        }
    }

    private void setComboBoxSelect(Combo target, String value) {
        for (int i = 0; i < models.size(); i++) {
            if (value.equals(models.get(i).name())) {
                target.select(i);
                return;
            }
        }
        target.clearSelection();
    }

    private void move(int delta) {
        Entry sel = getSelection();
        if (sel == null)
            return;
        int idx = entries.indexOf(sel);
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= entries.size())
            return;
        Collections.swap(entries, idx, newIdx);
        viewer.refresh();
        viewer.getTable().setSelection(newIdx);
    }

    private Entry getSelection() {
        ISelection sel = viewer.getSelection();
        if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
            Object o = ((IStructuredSelection) sel).getFirstElement();
            if (o instanceof Entry)
                return (Entry) o;
        }
        return null;
    }

    @Override
    public boolean performOk() {
        for (Entry e : entries) {
            if (!KEY_PATTERN.matcher(e.key).matches()) {
                setMessage("Ungültiger Key: Nur Buchstaben erlaubt.", IMessageProvider.ERROR);
                return false;
            }
        }

        int profileId = defaultProviderCombo.getSelectionIndex();
        if (profileId < 0) {
            config.setDefaultProfile(null);
        }
        else {
            config.setDefaultProfile(profiles.get(profileId));
        }

        if (defaultModelCombo.getSelectionIndex() >= 0) {
            config.setDefaultModel(models.get(defaultModelCombo.getSelectionIndex()).name());
        }
        else {
            config.setDefaultModel(null);
        }

        if (ccModelCombo.getSelectionIndex() >= 0) {
            config.setCcModel(models.get(ccModelCombo.getSelectionIndex()).name());
        }
        else {
            config.setCcModel(null);
        }

        config.setCcAllowThinking(ccAllowThink.getSelection());
        config.setCcAllowTools(ccAllowTools.getSelection());

        config.setPreferedLanguage(languageCombo.getText());
        config.setCcTimeout((long) ccTimeout.getSelection());

        IPreferenceStore store = config.store();
        store.setValue(PREF_COMPLETION, completionCheckbox.getSelection());
        store.setValue(PREF_LIMIT_CONTEXT, limitContextCheckbox.getSelection());
        store.setValue(PREF_CONTEXT_BEFORE, contextBeforeSpinner.getSelection());
        store.setValue(PREF_CONTEXT_AFTER, contextAfterSpinner.getSelection());
        store.setValue(PREF_ENTRIES, encode(entries));
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        languageCombo.select(0);
        defaultProviderCombo.select(0);
        defaultModelCombo.clearSelection();
        ccTimeout.setSelection(30);
        ccAllowThink.setSelection(false);
        ccAllowTools.setSelection(false);
        ccModelCombo.clearSelection();
        completionCheckbox.setSelection(false);
        limitContextCheckbox.setSelection(false);
        contextBeforeSpinner.setSelection(0);
        contextAfterSpinner.setSelection(0);
        entries.clear();
        viewer.refresh();
        super.performDefaults();
    }

    private void loadFromPrefs() {
        try {
            var defaultProfile = config.getDefaultProfile();
            if (defaultProfile == null) {
                defaultProviderCombo.clearSelection();
            }
            else {
                int id = profiles.indexOf(defaultProfile);
                defaultProviderCombo.select(id);
                reloadComboItems(true);
            }

            ccAllowThink.setSelection(config.getCcAllowThinking());
            ccAllowTools.setSelection(config.getCcAllowTools());

            languageCombo.setText(config.getPreferedLanguage().orElse(""));
            ccTimeout.setSelection(config.getCcTimeout().intValue());

            IPreferenceStore store = config.store();
            languageCombo.select(store.getDefaultInt(PREF_LANGUAGE));
            completionCheckbox.setSelection(store.getBoolean(PREF_COMPLETION));
            limitContextCheckbox.setSelection(store.getBoolean(PREF_LIMIT_CONTEXT));
            contextBeforeSpinner.setSelection(store.getInt(PREF_CONTEXT_BEFORE));
            contextAfterSpinner.setSelection(store.getInt(PREF_CONTEXT_AFTER));
            entries.clear();
            entries.addAll(decode(store.getString(PREF_ENTRIES)));
        }
        catch (IllegalArgumentException | NullPointerException ex) {
            log.error("error loading preferences: " + ex.getMessage());
        }
    }

    private static String encode(List<Entry> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Entry e = list.get(i);
            if (i > 0)
                sb.append(RECORD_SEP);
            sb.append(escape(e.key)).append(FIELD_SEP).append(escape(e.text));
        }
        return sb.toString();
    }

    private static List<Entry> decode(String raw) {
        List<Entry> out = new ArrayList<>();
        if (raw == null || raw.isEmpty())
            return out;
        String[] records = raw.split(RECORD_SEP, -1);
        for (String rec : records) {
            String[] parts = rec.split(FIELD_SEP, -1);
            String key = unescape(parts.length > 0 ? parts[0] : "");
            String text = unescape(parts.length > 1 ? parts[1] : "");
            out.add(new Entry(key, text));
        }
        return out;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace(FIELD_SEP, "\\" + FIELD_SEP).replace(RECORD_SEP, "\\" + RECORD_SEP);
    }

    private static String unescape(String s) {
        if (s == null)
            return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                out.append(s.charAt(i + 1));
                i++;
            }
            else {
                out.append(c);
            }
        }
        return out.toString();
    }

    // ---- Model ----
    private static class Entry {
        String key;
        String text;

        Entry(String key, String text) {
            this.key = Objects.requireNonNullElse(key, "");
            this.text = Objects.requireNonNullElse(text, "");
        }

        @Override
        public String toString() {
            return key + "=" + text;
        }
    }

    // ---- Editing Support ----
    private class KeyEditingSupport extends EditingSupport {
        private final TextCellEditor editor;

        KeyEditingSupport(TableViewer viewer) {
            super(viewer);
            editor = new TextCellEditor(viewer.getTable());
            // IMPORTANT: addVerifyListener exists on Text, not Control -> cast
            Text text = (Text) editor.getControl();
            text.addVerifyListener(new VerifyListener() {
                @Override
                public void verifyText(VerifyEvent e) {
                    String current = text.getText();
                    String newText = current.substring(0, e.start) + e.text + current.substring(e.end);
                    e.doit = newText.isEmpty() || KEY_PATTERN.matcher(newText).matches();
                }
            });
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    setMessage("Key: nur Buchstaben (A–Z, a–z)", IMessageProvider.INFORMATION);
                }
            });
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return ((Entry) element).key;
        }

        @Override
        protected void setValue(Object element, Object value) {
            String v = value == null ? "" : value.toString();
            if (!v.isEmpty() && !KEY_PATTERN.matcher(v).matches()) {
                MessageDialog.openError(getShell(), "Ungültiger Key", "Nur Buchstaben (A–Z, a–z) sind erlaubt.");
                return;
            }
            for (Entry other : entries) {
                if (other != element && other.key.equals(v)) {
                    MessageDialog.openError(getShell(), "Doppelter Key", "Der Key '" + v + "' ist bereits vorhanden.");
                    return;
                }
            }
            ((Entry) element).key = v;
            viewer.update(element, null);
        }
    }

    private class TextEditingSupport extends EditingSupport {
        private final TextCellEditor editor;

        TextEditingSupport(TableViewer viewer) {
            super(viewer);
            editor = new TextCellEditor(viewer.getTable()); // arbitrary text
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return ((Entry) element).text;
        }

        @Override
        protected void setValue(Object element, Object value) {
            ((Entry) element).text = value == null ? "" : value.toString();
            viewer.update(element, null);
        }
    }

}
