package com.github.kiu345.eclipse.eclipseai.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.prompt.PromptLoader;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.ECLIPSEAI_CONNECTION_TIMEOUT_SECONDS, 10);
        store.setDefault(PreferenceConstants.ECLIPSEAI_REQUEST_TIMEOUT_SECONDS, 30);
        store.setDefault(PreferenceConstants.ECLIPSEAI_KEEPALIVE_SECONDS, 600);

        store.setDefault(PreferenceConstants.ECLIPSEAI_BASE_URL, "http://localhost:11434");
        store.setDefault(PreferenceConstants.ECLIPSEAI_API_BASE_PATH, "/api");
        store.setDefault(PreferenceConstants.ECLIPSEAI_GET_MODEL_API_PATH, "/api/tags");
        store.setDefault(PreferenceConstants.ECLIPSEAI_API_KEY, "ollama");

        PromptLoader promptLoader = new PromptLoader();
        for (Prompts prompt : Prompts.values()) {
            store.setDefault(prompt.preferenceName(), promptLoader.getDefaultPrompt(prompt.getFileName()));
        }
    }
}
