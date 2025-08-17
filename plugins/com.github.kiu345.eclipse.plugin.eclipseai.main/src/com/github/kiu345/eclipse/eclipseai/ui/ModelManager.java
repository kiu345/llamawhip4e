package com.github.kiu345.eclipse.eclipseai.ui;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapter;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;

import jakarta.inject.Singleton;

@Creatable
@Singleton
public class ModelManager {

    private Instant lastRefreh = Instant.now();
    private List<ModelDescriptor> modelList = new CopyOnWriteArrayList<>();

    public synchronized List<ModelDescriptor> models(ChatAdapter adapter) {
        Instant now = Instant.now();
        if (now.until(lastRefreh, ChronoUnit.SECONDS) < 2 && !modelList.isEmpty()) {
            return modelList;
        }
        modelList.clear();
        modelList.addAll(adapter.getModels());
        return modelList;
    }
}
