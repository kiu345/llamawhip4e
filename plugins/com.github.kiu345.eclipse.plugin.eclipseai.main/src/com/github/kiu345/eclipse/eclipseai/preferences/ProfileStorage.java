package com.github.kiu345.eclipse.eclipseai.preferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileStorage {

    private static final String PREF_KEY = "ai.provider.profiles";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveProfiles(List<AIProviderProfile> profiles) throws IOException, BackingStoreException {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("my.plugin.id");
        String json = mapper.writeValueAsString(profiles);
        prefs.put(PREF_KEY, json);
        prefs.flush();
    }

    public static List<AIProviderProfile> loadProfiles() {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("my.plugin.id");
        String json = prefs.get(PREF_KEY, "[]");
        try {
            return mapper.readValue(json, new TypeReference<List<AIProviderProfile>>() {
            });
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
