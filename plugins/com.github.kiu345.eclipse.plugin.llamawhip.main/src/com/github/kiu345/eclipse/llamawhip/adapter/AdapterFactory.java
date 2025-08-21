package com.github.kiu345.eclipse.llamawhip.adapter;

import org.eclipse.core.runtime.ILog;

import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;

import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Factory for creating {@link ChatAdapter} instances for a specific AI provider.
 *
 * <p>
 * The factory supplies essential information about the adapter, validates
 * provider configurations, and creates adapter instances.
 * </p>
 *
 * @param <T> the concrete {@link ChatAdapter} type produced by this factory
 */
public interface AdapterFactory<T extends ChatAdapter<? extends StreamingChatModel>> {

    /**
     * Returns adapter information.
     */
    AdapterInfo info();

    /**
     * Provides the default profile for the AI provider.
     */
    AIProviderProfile defaultProfile();

    /**
     * Validates the given profile.
     *
     * @return an array of validation error messages, or an empty array if the profile is valid.
     */
    String[] validate(AIProviderProfile profile);

    /**
     * Creates a new adapter instance.
     */
    T create(ILog log, AIProviderProfile provider);
}
