package com.github.kiu345.eclipse.eclipseai.services;

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.kiu345.eclipse.eclipseai.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.eclipseai.adapter.ollama.OllamaAdapter.Config;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;

import jakarta.inject.Inject;

@Creatable
public class OllamaHttpClient {

    @Inject ILog log;
    
    @Inject
    private ClientConfiguration configuration;

    public List<ModelDescriptor> getModelList() {
        OllamaAdapter adapter = new OllamaAdapter(log, new Config(configuration));
        return adapter.getModels();
    }

    public ModelDescriptor getModel(String name) {
        OllamaAdapter adapter = new OllamaAdapter(log, new Config(configuration));
        return adapter.getModels().stream().filter(e -> e.name().equals(name)).findFirst().get();
    }
}
