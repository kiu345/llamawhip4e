package com.github.kiu345.eclipse.eclipseai.preferences;

import java.util.Arrays;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.adapter.local.LocalAIAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.openai.OpenAIAdapter;
import com.github.kiu345.eclipse.eclipseai.ui.pref.AIType;

public class ModelPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ComboFieldEditor modelSelector;

    private StringFieldEditor urlBaseEditor;
    private StringFieldEditor apiPathEditor;
    private StringFieldEditor modelApiPathEditor;
    private StringFieldEditor apiKeyEditor;
    private IntegerFieldEditor connectTimeoutEditor;
    private IntegerFieldEditor requestTimeoutEditor;
    private IntegerFieldEditor keepAliveEditor;

    private UISynchronize uiSync;
    private IPropertyChangeListener modelListener = e -> {
        if (PreferenceConstants.ECLIPSEAI_DEFINED_MODELS.equals(e.getProperty())) {
            uiSync.asyncExec(() -> {

            });
        }
    };

    public ModelPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Model API settings");

        getPreferenceStore().addPropertyChangeListener(modelListener);
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        String[][] entries = Arrays.stream(AIType.values()).map(e -> new String[] { e.toString(), e.name() }).toList().toArray(new String[][] {});

        Arrays.stream(getFieldEditorParent().getChildren()).forEach(Control::dispose);

        modelSelector = new ComboFieldEditor(PreferenceConstants.ECLIPSEAI_PROVIDER, "AI &provider:", entries, getFieldEditorParent());

        urlBaseEditor = new StringFieldEditor(PreferenceConstants.ECLIPSEAI_BASE_URL, "Base URL:", getFieldEditorParent());
        apiPathEditor = new StringFieldEditor(PreferenceConstants.ECLIPSEAI_API_BASE_PATH, "API base URL:", getFieldEditorParent());
        modelApiPathEditor = new StringFieldEditor(PreferenceConstants.ECLIPSEAI_GET_MODEL_API_PATH, "Model API Path:", getFieldEditorParent());
        apiKeyEditor = new StringFieldEditor(PreferenceConstants.ECLIPSEAI_API_KEY, "API Key:", getFieldEditorParent());
        connectTimeoutEditor = new IntegerFieldEditor(PreferenceConstants.ECLIPSEAI_CONNECTION_TIMEOUT_SECONDS, "Connect timeout:", getFieldEditorParent());
        requestTimeoutEditor = new IntegerFieldEditor(PreferenceConstants.ECLIPSEAI_REQUEST_TIMEOUT_SECONDS, "Request timeout:", getFieldEditorParent());
        keepAliveEditor = new IntegerFieldEditor(PreferenceConstants.ECLIPSEAI_KEEPALIVE_SECONDS, "Model KeepAlive time:", getFieldEditorParent());

        addField(modelSelector);
        addField(urlBaseEditor);
        addField(apiPathEditor);
        addField(modelApiPathEditor);
        addField(apiKeyEditor);

        addField(connectTimeoutEditor);
        addField(requestTimeoutEditor);
        addField(keepAliveEditor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        // workaroud to get UISynchronize as PreferencePage does not seem to
        // be handled by the eclipse context
        IEclipseContext eclipseContext = workbench.getService(IEclipseContext.class);
        uiSync = eclipseContext.get(UISynchronize.class);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == modelSelector) {
            String stringValue = event.getNewValue().toString();
            AIType value = AIType.valueOf(stringValue);
            switch (value) {
                case GITHUB:
                    urlBaseEditor.setEnabled(false, getFieldEditorParent());
                    apiPathEditor.setEnabled(false, getFieldEditorParent());
                    apiKeyEditor.setEnabled(true, getFieldEditorParent());
                    break;
                case LOCAL:
                    urlBaseEditor.setEnabled(true, getFieldEditorParent());
                    apiPathEditor.setEnabled(true, getFieldEditorParent());
                    apiKeyEditor.setEnabled(true, getFieldEditorParent());
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_BASE_URL, LocalAIAdapter.DEFAULT_URL);
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_API_BASE_PATH, "/v1");
                    break;
                case OLLAMA:
                    urlBaseEditor.setEnabled(true, getFieldEditorParent());
                    apiPathEditor.setEnabled(true, getFieldEditorParent());
                    apiKeyEditor.setEnabled(false, getFieldEditorParent());
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_BASE_URL, OllamaAdapter.DEFAULT_URL);
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_API_BASE_PATH, "/api");
                    break;
                case OPENAI:
                    urlBaseEditor.setEnabled(true, getFieldEditorParent());
                    apiPathEditor.setEnabled(true, getFieldEditorParent());
                    apiKeyEditor.setEnabled(true, getFieldEditorParent());
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_BASE_URL, OpenAIAdapter.DEFAULT_URL);
                    getPreferenceStore().setValue(PreferenceConstants.ECLIPSEAI_API_BASE_PATH, "/v1");
                    break;
                default:
                    urlBaseEditor.setEnabled(true, getFieldEditorParent());
                    apiPathEditor.setEnabled(true, getFieldEditorParent());
                    apiKeyEditor.setEnabled(true, getFieldEditorParent());
                    break;
            }
        }
        super.propertyChange(event);
    }

    @Override
    public void dispose() {
        getPreferenceStore().removePropertyChangeListener(modelListener);
        super.dispose();
    }

}
