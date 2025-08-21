package com.github.kiu345.eclipse.llamawhip.adapter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.di.annotations.Creatable;

import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class AdapterRegistry {

    private static final AdapterRegistry instance = new AdapterRegistry();

    private Map<String, AdapterFactory<ChatAdapter<? extends StreamingChatModel>>> registeredAdapters = new HashMap<>(5);

    public static AdapterRegistry instance() {
        return instance;
    }

    public void register(String name, Class<? extends AdapterFactory<ChatAdapter<? extends StreamingChatModel>>> factoryClass) {
        try {
            registeredAdapters.put(name, factoryClass.getDeclaredConstructor().newInstance());
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("invalid class provided", e);
        }
    }

    public void deregister(String name) {
        registeredAdapters.remove(name);
    }

    public AdapterFactory<ChatAdapter<? extends StreamingChatModel>> get(String name) {
        return registeredAdapters.get(name);
    }

    public AdapterInfo info(String name) {
        return registeredAdapters.get(name).info();
    }
}
