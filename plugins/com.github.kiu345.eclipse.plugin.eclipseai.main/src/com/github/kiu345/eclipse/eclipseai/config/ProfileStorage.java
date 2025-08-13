package com.github.kiu345.eclipse.eclipseai.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.osgi.service.prefs.BackingStoreException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kiu345.eclipse.eclipseai.Activator;

import jakarta.inject.Singleton;

/**
 * Utility class for persisting and retrieving {@link AIProviderProfile} objects
 * in the Eclipse preference store.
 *
 * <p>
 * The class uses a JSON representation of the profiles list, stored under
 * the key {@code ai.provider.profiles}. All operations are static and
 * thread‑safe because the underlying {@link ObjectMapper} is immutable.
 * </p>
 *
 * <p>
 * Typical usage pattern:
 * </p>
 * 
 * <pre>
 * // Load all profiles
 * List&lt;AIProviderProfile&gt; profiles = ProfileStorage.loadProfiles();
 *
 * // Add or update a profile
 * ProfileStorage.storeProfile(newProfile);
 *
 * // Retrieve a single profile by its UUID
 * AIProviderProfile profile = ProfileStorage.getProfile(profileId);
 * </pre>
 *
 * @author kiu
 */
@Creatable
@Singleton
public class ProfileStorage {

    /** Shared {@link ObjectMapper} instance for JSON (de)serialization. */
    private static final ObjectMapper mapper = new ObjectMapper();

    private static IEclipsePreferences getStorage() {
        return InstanceScope.INSTANCE.getNode(Activator.getBundleContext().getBundle().getSymbolicName());
    }

    /**
     * Persists the given list of {@link AIProviderProfile} objects to the
     * Eclipse preference store.
     */
    public static void saveProfiles(List<AIProviderProfile> profiles) throws IOException, BackingStoreException {
        IEclipsePreferences prefs = getStorage();
        String json = mapper.writeValueAsString(profiles);
        prefs.put(PreferenceConstants.PROFILES, json);
        prefs.flush();
    }

    /**
     * Loads all {@link AIProviderProfile} objects from the preference store.
     *
     * @return a list of profiles; never {@code null}
     */
    public static List<AIProviderProfile> loadProfiles() {
        IEclipsePreferences prefs = getStorage();
        String json = prefs.get(PreferenceConstants.PROFILES, "[]");
        try {
            return mapper.readValue(json, new TypeReference<List<AIProviderProfile>>() {
            });
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a single profile by its {@link UUID}.
     */
    public static AIProviderProfile getProfile(UUID id) {
        if (id == null) {
            return null;
        }

        return loadProfiles().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Stores or updates a single {@link AIProviderProfile} in the preference store.
     *
     * <p>
     * If a profile with the same UUID already exists, it is replaced;
     * otherwise the profile is appended to the list.
     * </p>
     */
    public static void storeProfile(AIProviderProfile profile) throws IOException, BackingStoreException {
        List<AIProviderProfile> profiles = loadProfiles();

        boolean replaced = false;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(profile.getId())) {
                profiles.set(i, profile);
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            profiles.add(profile);
        }

        saveProfiles(profiles);
    }

    /**
     * Removes an {@link AIProviderProfile} from the preference store if a profile
     * with the specified {@link UUID} exists.
     */
    public static void removeProfile(UUID id) throws IOException, BackingStoreException {
        if (id == null) {
            return;
        }

        List<AIProviderProfile> profiles = loadProfiles();
        boolean removed = profiles.removeIf(p -> p.getId().equals(id));

        if (removed) {
            saveProfiles(profiles);
        }
    }

}
