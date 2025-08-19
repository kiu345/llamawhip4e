package com.github.kiu345.eclipse.llamawhip.adapter;

import org.eclipse.core.runtime.ILog;

import com.github.kiu345.eclipse.llamawhip.adapter.local.LocalAIAdapter;
import com.github.kiu345.eclipse.llamawhip.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.llamawhip.adapter.openai.OpenAIAdapter;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;

import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Factory for {@link ChatAdapter} instances.
 *
 * <p>
 * Depending on the {@link AIProviderProfile#getProvider()} value, the factory
 * returns an appropriate implementation:
 * <ul>
 * <li>{@link GithubCopilotAdapter} for {@code GitHub Copilot}</li>
 * <li>{@link LocalAIAdapter} for {@code LocalAI Server}</li>
 * <li>{@link OllamaAdapter} for {@code Ollama}</li>
 * <li>{@link OpenAIAdapter} for {@code ChatGPT}</li>
 * </ul>
 *
 * @see AIProviderProfile
 * @see ChatAdapter
 */
public class ChatAdapterFactory {
    public static String[] validate(AIProviderProfile provider) {
        switch (provider.getProvider()) {
//            case GITHUB_COPILOT:
//                return GithubCopilotAdapter.validate(provider);
            case LOCALAI:
                return LocalAIAdapter.validate(provider);
            case OLLAMA:
                return OllamaAdapter.validate(provider);
            case OPENAI:
                return OpenAIAdapter.validate(provider);
            default:
                throw new IllegalArgumentException("undefined provider type " + provider.getProvider());
        }
    }

    public static ChatAdapter<? extends StreamingChatModel> create(ILog log, AIProviderProfile provider) {
        switch (provider.getProvider()) {
//            case GITHUB_COPILOT:
//                return new GithubCopilotAdapter(log, provider);
            case LOCALAI:
                return new LocalAIAdapter(log, provider);
            case OLLAMA:
                return new OllamaAdapter(log, provider);
            case OPENAI:
                return new OpenAIAdapter(log, provider);
            default:
                throw new IllegalArgumentException("undefined provider type " + provider.getProvider());

        }
    }
}
