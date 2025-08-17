package com.github.kiu345.eclipse.eclipseai.services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.kiu345.eclipse.eclipseai.subscribers.AppendMessageToViewSubscriber;
import com.github.kiu345.eclipse.eclipseai.subscribers.PrintMessageSubscriber;

@Creatable
@Singleton
public class OllamaHttpClientProvider {
    @Inject
    private Provider<AIStreamJavaHttpClient> clientProvider;
    @Inject
    private AppendMessageToViewSubscriber appendMessageToViewSubscriber;
    @Inject
    private PrintMessageSubscriber printMessageSubscriber;

    public AIStreamJavaHttpClient get() {
        AIStreamJavaHttpClient client = clientProvider.get();
        client.subscribe(printMessageSubscriber);
        client.subscribe(appendMessageToViewSubscriber);
        return client;
    }
}
