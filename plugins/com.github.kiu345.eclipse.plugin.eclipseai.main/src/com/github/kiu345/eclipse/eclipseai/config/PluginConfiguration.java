package com.github.kiu345.eclipse.eclipseai.config;

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

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class PluginConfiguration {

    public static final boolean DEBUG_MODE = true;

    private IPreferenceStore preferenceStore;

    @Inject
    private ProfileStorage profileStorage;

    private List<ModelDescriptor> modelList;

    public PluginConfiguration() {
        preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.getDefault().getBundle().getSymbolicName());
    }

    @PostConstruct
    public void init() {
    }

    private static PluginConfiguration config;

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

    public ProfileStorage getProfileStorage() {
        return profileStorage;
    }

    public AIProviderProfile getDefaultProfile() {
        String profileUuid = preferenceStore.getString(PreferenceConstants.DEFAULT_PROVIDER);
        if (StringUtils.isAllBlank(profileUuid)) {
            return null;
        }
        UUID id = UUID.fromString(profileUuid);
        return ProfileStorage.getProfile(id);
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

    public Optional<String> getPreferedLanguage() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL));
    }

    public void setPreferedLanguage(String model) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL, model);
    }

    public Optional<String> getSelectedModel() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL));
    }

    public void setSelectedModel(String model) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL, model);
    }

//    public Optional<Integer> getTemperature() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return Optional.of(prefernceStore.getInt(PreferenceConstants.ECLIPSEAI_TEMPERATURE));
//    }
//
//    public void setTemperature(Integer temperature) {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_TEMPERATURE, temperature);
//
//    }
//
//    public Optional<Boolean> getUseFunctions() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return Optional.of(prefernceStore.getBoolean(PreferenceConstants.ECLIPSEAI_USEFUNCTIONS));
//    }
//
//    public void setUseFunctions(Boolean useFunctions) {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_USEFUNCTIONS, useFunctions);
//    }
//
//    public Optional<Boolean> getWebAccess() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return Optional.of(prefernceStore.getBoolean(PreferenceConstants.ECLIPSEAI_WEBACCESS));
//    }
//
//    public void setWebAccess(Boolean useFunctions) {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_WEBACCESS, useFunctions);
//    }

//    public String getApiKey() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return prefernceStore.getString(PreferenceConstants.ECLIPSEAI_API_KEY);
//    }

//    public String getBaseUrl() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return prefernceStore.getString(PreferenceConstants.ECLIPSEAI_BASE_URL);
//    }

//    public String getApiBaseUrl() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return getBaseUrl() + prefernceStore.getString(PreferenceConstants.ECLIPSEAI_API_BASE_PATH);
//    }
//
//    public String getModelApiPath() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return getBaseUrl() + prefernceStore.getString(PreferenceConstants.ECLIPSEAI_GET_MODEL_API_PATH);
//    }

//    public int getConnectionTimoutSeconds() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_CONNECTION_TIMEOUT_SECONDS));
//    }
//
//    public int getRequestTimoutSeconds() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_REQUEST_TIMEOUT_SECONDS));
//    }
//
//    public Integer getKeepAliveSeconds() {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        String value = prefernceStore.getString(PreferenceConstants.ECLIPSEAI_KEEPALIVE_SECONDS);
//        if (StringUtils.isBlank(value)) {
//            return null;
//        }
//        return Integer.parseInt(value);
//    }
}
