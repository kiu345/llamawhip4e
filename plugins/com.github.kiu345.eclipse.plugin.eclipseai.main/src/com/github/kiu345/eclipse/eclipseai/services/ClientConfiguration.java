package com.github.kiu345.eclipse.eclipseai.services;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;
import com.github.kiu345.eclipse.eclipseai.preferences.PreferenceConstants;

import jakarta.inject.Singleton;

@Creatable
@Singleton
public class ClientConfiguration {
    
    public static final boolean DEBUG_MODE=true;
    
    private List<ModelDescriptor> modelList;

    public void setModelList(List<ModelDescriptor> modelList) {
        this.modelList = modelList;
    }

    public Optional<ModelDescriptor> getModelDescriptor(String modelName) {
        return modelList.stream().filter(t -> t.name().equals(modelName)).findFirst();
    }

    public Optional<String> getSelectedModel() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL));
    }

    public void setSelectedModel(String model) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_SELECTED_MODEL, model);
    }

    public Optional<Integer> getTemperature() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getInt(PreferenceConstants.ECLIPSEAI_TEMPERATURE));
    }

    public void setTemperature(Integer temperature) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_TEMPERATURE, temperature);

    }

    public Optional<Boolean> getUseFunctions() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getBoolean(PreferenceConstants.ECLIPSEAI_USEFUNCTIONS));
    }

    public void setUseFunctions(Boolean useFunctions) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_USEFUNCTIONS, useFunctions);
    }

    public Optional<Boolean> getWebAccess() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Optional.of(prefernceStore.getBoolean(PreferenceConstants.ECLIPSEAI_WEBACCESS));
    }

    public void setWebAccess(Boolean useFunctions) {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        prefernceStore.setValue(PreferenceConstants.ECLIPSEAI_WEBACCESS, useFunctions);
    }

    public String getApiKey() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return prefernceStore.getString(PreferenceConstants.ECLIPSEAI_API_KEY);
    }

    public String getBaseUrl() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return prefernceStore.getString(PreferenceConstants.ECLIPSEAI_BASE_URL);
    }

    public String getApiBaseUrl() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return getBaseUrl() + prefernceStore.getString(PreferenceConstants.ECLIPSEAI_API_BASE_PATH);
    }

    public String getModelApiPath() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return getBaseUrl() + prefernceStore.getString(PreferenceConstants.ECLIPSEAI_GET_MODEL_API_PATH);
    }

    public int getConnectionTimoutSeconds() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_CONNECTION_TIMEOUT_SECONDS));
    }

    public int getRequestTimoutSeconds() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.ECLIPSEAI_REQUEST_TIMEOUT_SECONDS));
    }

    public Integer getKeepAliveSeconds() {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        String value = prefernceStore.getString(PreferenceConstants.ECLIPSEAI_KEEPALIVE_SECONDS);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
