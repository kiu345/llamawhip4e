package com.github.kiu345.eclipse.llamawhip.config;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Main configuration storage handling for LlamaWhip.
 * <p>
 * This singleton class provides centralized access to the preference store,
 * profile storage, and model settings. It exposes convenience methods for
 * retrieving and updating user preferences such as the default language,
 * default provider, and code completion settings. The instance can be
 * obtained via {@link #instance()}.
 */
@Creatable
@Singleton
public class PluginConfiguration {

    private IPreferenceStore preferenceStore;

    @Inject
    private AIProfileStorage profileStorage;

    private List<ModelDescriptor> modelList;

    public PluginConfiguration() {
        preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.getDefault().getBundle().getSymbolicName());
    }

    @PostConstruct
    public void init() {
    }

    private static PluginConfiguration config;

    public static void inject(PluginConfiguration newConfig) {
        config = newConfig;
    }

    public static PluginConfiguration instance() {
        if (config == null) {
            IEclipseContext context = EclipseContextFactory.getServiceContext(
                    FrameworkUtil.getBundle(Activator.class).getBundleContext()
            );
            config = context.get(PluginConfiguration.class);
        }
        return config;
    }

    public IPreferenceStore store() {
        return preferenceStore;
    }

    public AIProfileStorage getProfileStorage() {
        return profileStorage;
    }

    public Optional<String> getPreferedLanguage() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getString(PreferenceConstants.LANGUAGE));
    }

    public void setPreferedLanguage(String model) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.LANGUAGE, model);
    }

    public AIProviderProfile getDefaultProfile() {
        String profileUuid = preferenceStore.getString(PreferenceConstants.DEFAULT_PROVIDER);
        if (StringUtils.isAllBlank(profileUuid)) {
            return null;
        }
        UUID id = UUID.fromString(profileUuid);
        return AIProfileStorage.getProfile(id);
    }

    public void setDefaultProfile(AIProviderProfile profile) {
        if (profile == null || profile.getId() == null) {
            preferenceStore.setValue(PreferenceConstants.DEFAULT_PROVIDER, "");
        }
        else {
            preferenceStore.setValue(PreferenceConstants.DEFAULT_PROVIDER, profile.getId().toString());
        }
    }

    public String getDefaultModel() {
        String name = preferenceStore.getString(PreferenceConstants.DEFAULT_MODEL);
        return StringUtils.isAllBlank(name) ? null : name;
    }

    public void setDefaultModel(String name) {
        if (StringUtils.isBlank(name)) {
            preferenceStore.setValue(PreferenceConstants.DEFAULT_MODEL, "");
        }
        else {
            preferenceStore.setValue(PreferenceConstants.DEFAULT_MODEL, name);
        }
    }

    public Long getCcTimeout() {
        return preferenceStore.getLong(PreferenceConstants.CC_TIMEOUT);
    }

    public void setCcTimeout(Long value) {
        preferenceStore.setValue(PreferenceConstants.CC_TIMEOUT, value);
    }

    public boolean getCcAllowThinking() {
        return preferenceStore.getBoolean(PreferenceConstants.CC_THINK);
    }

    public void setCcAllowThinking(boolean value) {
        preferenceStore.setValue(PreferenceConstants.CC_THINK, value);
    }

    public boolean getCcAllowTools() {
        return preferenceStore.getBoolean(PreferenceConstants.CC_TOOLS);
    }

    public void setCcAllowTools(boolean value) {
        preferenceStore.setValue(PreferenceConstants.CC_TOOLS, value);
    }

    public String getCcModel() {
        String name = preferenceStore.getString(PreferenceConstants.CC_MODEL);
        return StringUtils.isAllBlank(name) ? null : name;
    }

    public void setCcModel(String name) {
        if (StringUtils.isBlank(name)) {
            preferenceStore.setValue(PreferenceConstants.CC_MODEL, "");
        }
        else {
            preferenceStore.setValue(PreferenceConstants.CC_MODEL, name);
        }
    }

    public void setModelList(List<ModelDescriptor> modelList) {
        this.modelList = modelList;
    }

    public Optional<ModelDescriptor> getModelDescriptor(String modelName) {
        return modelList.stream().filter(t -> t.name().equals(modelName)).findFirst();
    }
}
