package com.github.kiu345.eclipse.llamawhip.adapter.jlama;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;

import com.github.kiu345.eclipse.llamawhip.adapter.AdapterFactory;
import com.github.kiu345.eclipse.llamawhip.adapter.AdapterInfo;
import com.github.kiu345.eclipse.llamawhip.adapter.AdapterInfo.Type;
import com.github.kiu345.eclipse.llamawhip.config.AIProvider;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;

/**
 * Factory for creating {@link JLamaAdapter} instances.
 */
public class JLamaFactory implements AdapterFactory<JLamaAdapter> {

    @Override
    public AdapterInfo info() {
        return new AdapterInfo(
                "JLama",
                "JVM interferencing",
                "https://github.com/tjake/Jlama",
                "0.5.0-beta",
                Set.of(Type.FILE)
        );
    }

    @Override
    public AIProviderProfile defaultProfile() {
        return AIProviderProfile.builder()
                .provider(AIProvider.JLAMA)
                .modelNames("tjake/Yi-Coder-1.5B-Chat-JQ4,tjake/codegemma-2b-JQ4,tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4,tjake/Llama-3.2-1B-Instruct-JQ4")
                .build();
    }

    @Override
    public String[] validate(AIProviderProfile profile) {
        ArrayList<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(profile.getUrlBase())) {
            errors.add("no model names");
        }
        return errors.toArray(String[]::new);
    }

    @Override
    public JLamaAdapter create(ILog log, AIProviderProfile provider) {
        return new JLamaAdapter(log, provider);
    }

}
