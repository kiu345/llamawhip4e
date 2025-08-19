package com.github.kiu345.eclipse.llamawhip.config;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        IPreferenceStore store = PluginConfiguration.instance().store();

        store.setDefault(PreferenceConstants.LANGUAGE, "");
        store.setDefault(PreferenceConstants.THEME, UITheme.LIGHT.name());

        PromptLoader promptLoader = new PromptLoader(Platform.getLog(Activator.getDefault().getBundle()));
        for (Prompts prompt : Prompts.values()) {
            store.setDefault(prompt.preferenceName(), promptLoader.getDefaultPrompt(prompt));
        }
    }
}
