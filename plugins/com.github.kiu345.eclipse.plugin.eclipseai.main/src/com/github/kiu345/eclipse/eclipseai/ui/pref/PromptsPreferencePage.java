package com.github.kiu345.eclipse.eclipseai.ui.pref;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.config.PluginConfiguration;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;

public class PromptsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private List list;
    private Text textArea;
    private IPreferenceStore preferenceStore;
    private Label infoText;

    public PromptsPreferencePage() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle("Prompts");
    }

    @Override
    public void init(IWorkbench workbench) {
        preferenceStore = PluginConfiguration.instance().store();
    }

    @Override
    protected Control createContents(Composite parent) {
        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        list = new List(sashForm, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
      
        infoText = new Label(sashForm, SWT.WRAP);
        infoText.setText("");

        textArea = new Text(sashForm, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        var textAreaLayoutData = new GridData(GridData.FILL_BOTH);
        textArea.setLayoutData(textAreaLayoutData);
        

        // Sets the initial weight ratio
        sashForm.setWeights(new int[] { 15, 5, 80 });

        initializeListeners();

//        preferencePresenter.registerView(this);
        String[] prompts = Arrays.stream(Prompts.values()).map(Prompts::getDescription).toArray(String[]::new);
        setPrompts(prompts);
        

        return sashForm;
    }

    private void initializeListeners() {
        list.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = list.getSelectionIndex();
                setSelectedPrompt(selectedIndex);
            }
        });
    }

    public void setPrompts(String[] prompts) {
        if (prompts == null) {
            prompts = new String[] {};
        }
        list.setItems(prompts);
    }

    public void setCurrentPrompt(String selectedItem) {
        textArea.setText(selectedItem);
    }

    @Override
    protected void performApply() {
        // Save the current prompt text to the preference store
        int selectedIndex = list.getSelectionIndex();
        if (selectedIndex != -1) {
            savePrompt(selectedIndex, textArea.getText());
        }
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        int selectedIndex = list.getSelectionIndex();
        if (selectedIndex != -1) {
            resetPrompt(selectedIndex);
        }
        super.performDefaults();
    }

    public void setSelectedPrompt(int index) {
        if (index < 0) {
            setCurrentPrompt("");
        }
        else {
            var prompt = preferenceStore.getString(getPreferenceName(index));
            setCurrentPrompt(prompt);
            textArea.setEditable(index != 0);
            notes(index);
        }
    }

    private void notes(int index) {
        switch (Prompts.values()[index]) {
            case BASE -> {
                infoText.setText("This base prompt cannot be edited, it is a wrapper for other prompts to inject some system parameters");
            }
            case SYSTEM -> {
                infoText.setText("This prompt defines how the agent respones on a global level.");
            }
            default -> {
                infoText.setText("");
            }
        }
    }

    private String getPreferenceName(int index) {
        return Prompts.values()[index].preferenceName();
    }

    public void savePrompt(int selectedIndex, String text) {
        preferenceStore.setValue(getPreferenceName(selectedIndex), text);
    }

    public void resetPrompt(int selectedIndex) {
        var propertyName = getPreferenceName(selectedIndex);
        var defaultValue = preferenceStore.getDefaultString(propertyName);
        preferenceStore.setValue(propertyName, defaultValue);
        setCurrentPrompt(defaultValue);
    }

}
